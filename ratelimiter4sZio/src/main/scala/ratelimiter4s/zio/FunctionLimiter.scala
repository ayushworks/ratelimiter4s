package ratelimiter4s.zio

import io.github.resilience4j.ratelimiter.{ RateLimiter, RequestNotPermitted }
import ratelimiter4s.core.{ Function0Limiter, Function1Limiter }
import zio.{ IO, Task, ZIO }

/**
 * @author Ayush Mittal
 */
class ZFunction0Limiter[A](rateLimiter: RateLimiter, function0: () => A) extends (() => Task[A]) {
  def pure: IO[RequestNotPermitted, A] =
    ZIO.fromEither {
      Function0Limiter.pure(rateLimiter, function0)
    }

  def apply: Task[A] =
    ZIO.effect(Function0Limiter(rateLimiter, function0)).flatMap {
      case Left(error) => ZIO.fail(error)
      case Right(a)    => ZIO.succeed(a)
    }
}

class ZFunction1Limiter[A, T](rateLimiter: RateLimiter, function1: T => A) extends (T => Task[A]) {
  def pure(t: T): IO[RequestNotPermitted, A] =
    ZIO.fromEither {
      Function1Limiter.pure(rateLimiter, function1, t)
    }

  def apply(t: T): Task[A] =
    ZIO.effect(Function1Limiter(rateLimiter, function1, t)).flatMap {
      case Left(error) => ZIO.fail(error)
      case Right(a)    => ZIO.succeed(a)
    }
}
