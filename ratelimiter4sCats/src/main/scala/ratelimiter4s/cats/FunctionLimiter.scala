package ratelimiter4s.cats

import cats.data.EitherT
import cats.effect.IO
import io.github.resilience4j.ratelimiter.{ RateLimiter, RequestNotPermitted }
import ratelimiter4s.core.{ Function0Limiter, Function1Limiter }

/**
 * @author Ayush Mittal
 */
class CatsFunction0Limiter[A](rateLimiter: RateLimiter, function0: () => A) extends (() => EitherT[IO, Throwable, A]) {
  def pure: EitherT[IO, RequestNotPermitted, A] =
    EitherT.fromEither {
      Function0Limiter.pure(rateLimiter, function0)
    }

  def apply: EitherT[IO, Throwable, A] =
    EitherT(IO(Function0Limiter(rateLimiter, function0)))
}

class CatsFunction1Limiter[A, T](rateLimiter: RateLimiter, function1: T => A) extends (T => EitherT[IO, Throwable, A]) {
  def pure(t: T): EitherT[IO, RequestNotPermitted, A] =
    EitherT.fromEither {
      Function1Limiter.pure(rateLimiter, function1, t)
    }

  def apply(t: T): EitherT[IO, Throwable, A] =
    EitherT(IO(Function1Limiter(rateLimiter, function1, t)))
}
