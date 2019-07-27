package ratelimiter4s.zio

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */
object ZIORateLimiter {

  implicit class Function0Decorator[A](function0: () => A) {
    def decorate(implicit rateLimiter: RateLimiter): ZFunction0Limiter[A] =
      new ZFunction0Limiter[A](rateLimiter, function0)
  }

  implicit class Function1Decorator[A, T](function1: T => A) {
    def decorate(implicit rateLimiter: RateLimiter): ZFunction1Limiter[A, T] =
      new ZFunction1Limiter[A, T](rateLimiter, function1)
  }

}
