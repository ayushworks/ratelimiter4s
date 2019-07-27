# Rate limiter library designed for Scala.

[![Build Status](https://travis-ci.com/ayushworks/pariksha.svg?branch=master)](https://travis-ci.com/ayushworks/pariksha)

**ratelimter4s** is lightweight rate limiter library designed for Scala. It provides decorators to enhance 
any `Function` with rate limiting capabilities. Is also provides support to decorate a `Function` with rate limiting capabilities and produce output where values are instances of  `cats-effect` and `zio` effect types.

ratelimiter4s uses [resilience4j rate limiter](https://resilience4j.readme.io/docs/ratelimiter) to create the
underlying rate limiting policies. Resilience4j is a lightweight fault tolerance library inspired by [Netflix Hystrix](https://github.com/Netflix/Hystrix), but designed for Java 8.

The hero of our story is the `FunctionDecorator` **class**  which decorates instances of `Function` by using 
an implicit `RateLimiterConfig`. Any `Function` which returns a type `A` can be decorated. The decorated method returns an `Either[RequestNotPermitted, A]` with the `Left` indicating failed request due to rate limiting and `Right` the presence of a successful response


Consider a public service which takes a character name and returns an `Artist`.
```scala
trait ArtistService {
  def getArtist(character: String): Artist 
}
```

Rules dictate that calling rate for `getArtist` to be not higher than 3 requests/second.

We can define a `RateLimiter` config defined like this

```scala
implicit val tenPerMillis: RateLimiterConfig = RateLimiterConfig.custom()
                      .limitRefreshPeriod(Duration.ofSeconds(1))
                      .limitForPeriod(3)
                      .timeoutDuration(Duration.ofMillis(25))
                      .build()
```
The details of the limiter config are described in detail [here](https://resilience4j.readme.io/docs/ratelimiter)

We can use this config and decorate our service. We would need to import the `FRateLimiter` class that provides instances of `FunctionDecorator`

```scala
import ratelimiter4s.core.FRateLimiter._

val rateLimitedService = (getArtist _).decorate 
```

we can also say

```scala
import ratelimiter4s.core.FRateLimiter._

def rateLimitedService = (getArtist _).decorate
```

Lets checkout a rate limited service in action

```scala
//first attempt
rateLimitedService("Sheldon") == Right(User("Jim Parsons"))
rateLimitedService("Penny")   == Right(User("Kaley Cuoco"))
rateLimitedService("Leonard") == Right(User("Johnny Galecki"))

//fourth attempt will fail with a  RequestNotPermitted 
rateLimitedService("Kripke") == Left(RequestNotPermitted)
```
**And that is it! We have achieved the objective of limiting the requests to 3/second.** 


`decorate` methods return a `ratelimiter4s.core.FunctionNLimiter` instances. To be more accurate:

*  decorating a `scala.Function0` returns a `Function0Limiter` 
*  decorating a `scala.Function1` returns a `Function1Limiter` 
*  and so on  for N till 22

#### Pure and Impure 

A pure scala function is side-effect free, plus the result does not depend on anything other than its inputs.

`FunctionNLimiter` provides a pure apply for every decorated function. The return type of `pure`
is `Either[RequestNotPermitted,A]`.

```scala
def greeter(guest: String) : String = s"Hello $guest"
```
Decorating `greeter` with a rate limiter

```scala
val rateLimitedGreeter = (greeter _).decorate 
```

We can call `pure` on the rate limited method.

```scala
val result: Either[RequestNotPermitted, String] = rateLimitedGreeter.pure("World")
```

Any exceptions thrown by the original `greeter` method are unhandled. So any instances of  `Left` can only have a `RequestNotPermitted` type inside.

However many useful methods that we would like to decorate interact with outside world, mutate state and 
would not qualify as pure functions. Such a rate limited method could throw exceptions which are caused by the actual business logic. 

Consider the following example as a tweak to the original `greeter`.
```scala
def greeter(guest: String) : String = {
  
  if(!guestList.contains(guest)) throw new UninvitedGuestException(guest)
  
  s"Hello $guest"
}
```

Decorating the impure `greeter` with a rate limiter would remain the same.

```scala
val rateLimitedGreeter = (greeter _).decorate 
```

**We should call an `apply` on the rate limited method instead of pure**. This means that we are also expecting `greeter` to fail for reasons other than rate limitations.

```scala
val result: Either[Throwable, String] = rateLimitedGreeter("World")
```
The `Left` is of `Throwable` type in this case which is self-explanatory.
 
#### Support for cats effect

We can also capture the result of a rate limited method using [cats](https://typelevel.org/cats/) and its monad transformer instance `EitherT` 

Consider an image recognition service.

```scala
trait RecognitionService {
  def recognizeImage(image: URL): ImageType 
}
```

Lets decorate the `recognizeImage` method using `CatsFunctionLimiter` instances available in the `CatsRateLimiter` **class**

```scala
import ratelimiter4s.cats.CatsRateLimiter._

val rateLimitedService = (recognizeImage _).decorate 
``` 

Calling this rate limited return an `EitherT` bounded to an `IO` effect type

```scala
val result : EitherT[IO, Throwable, ImageType] = rateLimitedService("https://samples.clarifai.com/metro-north.jpg")
```

The result is an `EitherT[IO, Throwable, ImageType`] . The `left` is a throwable because the method can fail due to other reasons apart from `RequestNotPermitted`. If that is not the case, we can use `pure`.

```scala
val result : EitherT[IO, RequestNotPermitted, ImageType] = rateLimitedService.pure("https://samples.clarifai.com/metro-north.jpg")
```

the type clearly indicates that the failure can only be caused by a `RequestNotPermitted` error.

#### Support for ZIO :

We can also capture the result of a rate limited method using [zio](https://zio.dev/) types. 

Lets consider the  image recognition service again.

```scala
trait RecognitionService {
  def recognizeImage(image: URL): ImageType 
}
```

We decorate this time using the `ZFunctionLimiter` instances available inside the `ZIORateLimiter` **class**.

```scala
import ratelimiter4s.zio.ZIORateLimiter._

val rateLimitedService = (recognizeImage _).decorate 
``` 

Calling this rate limited return an `Task[ImageType]` result which is shorthand for `ZIO[Any, Throwable, ImageType]`

```scala
val result : Task[ImageType] = rateLimitedService("https://samples.clarifai.com/metro-north.jpg")
```

The result is `Task` type because the method can fail due to other reasons apart from `RequestNotPermitted`. If that is not the case , we can use `pure`.

```scala
val result : IO[RequestNotPermitted, ImageType] = rateLimitedService.pure("https://samples.clarifai.com/metro-north.jpg")
```

`IO[RequestNotPermitted, ImageType]` which is a shorthand for `ZIO[Any, RequestNotPermitted, ImageType]`  clearly indicates that the failure can only be caused by a `RequestNotPermitted` error.

