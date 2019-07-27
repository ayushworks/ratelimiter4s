package ratelimiter4s.core

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */

object FRateLimiter {

  implicit class Function0Decorator[A](function0: () => A) {
    def decorate(implicit rateLimiter: RateLimiter): Function0Limiter[A] =
      new Function0Limiter[A](rateLimiter, function0)
  }

  implicit class Function1Decorator[A, T](function1: T => A) {
    def decorate(implicit rateLimiter: RateLimiter): Function1Limiter[A, T] =
      new Function1Limiter[A, T](rateLimiter, function1)
  }
}
