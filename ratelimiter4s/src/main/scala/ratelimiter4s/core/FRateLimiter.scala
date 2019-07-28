package ratelimiter4s.core

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */

object FRateLimiter {

  def limit[A](function0: () => A, rateLimiter: RateLimiter): Function0Limiter[A] =
      new Function0Limiter[A](rateLimiter, function0)

  def limit[A,B](function1: B => A, rateLimiter: RateLimiter): Function1Limiter[A, B] =
      new Function1Limiter[A, B](rateLimiter, function1)

}
