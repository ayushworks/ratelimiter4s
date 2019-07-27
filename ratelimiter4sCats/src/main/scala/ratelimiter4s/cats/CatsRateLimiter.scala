package ratelimiter4s.cats

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */
object CatsRateLimiter {

  implicit class Function0Decorator[A](function0: () => A) {
    def decorate(implicit rateLimiter: RateLimiter): CatsFunction0Limiter[A] =
      new CatsFunction0Limiter[A](rateLimiter, function0)
  }

  implicit class Function1Decorator[A, T](function1: T => A) {
    def decorate(implicit rateLimiter: RateLimiter): CatsFunction1Limiter[A, T] =
      new CatsFunction1Limiter[A, T](rateLimiter, function1)
  }
}
