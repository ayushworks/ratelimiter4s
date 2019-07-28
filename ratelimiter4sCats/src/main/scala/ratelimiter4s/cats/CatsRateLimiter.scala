package ratelimiter4s.cats

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */
object CatsRateLimiter {

  def limit[A](function0: () => A, rateLimiter: RateLimiter): CatsFunction0Limiter[A] =
      new CatsFunction0Limiter[A](rateLimiter, function0)

  def limit[A,B](function1: B => A, rateLimiter: RateLimiter): CatsFunction1Limiter[A, B] =
      new CatsFunction1Limiter[A, B](rateLimiter, function1)

}
