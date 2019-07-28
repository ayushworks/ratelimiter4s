package ratelimiter4s.zio

import io.github.resilience4j.ratelimiter.RateLimiter

/**
 * @author Ayush Mittal
 */
object ZIORateLimiter {

  def limit[A](function0: () => A, rateLimiter: RateLimiter): ZFunction0Limiter[A] =
      new ZFunction0Limiter[A](rateLimiter, function0)


  def limit[A,B](function1: B => A, rateLimiter: RateLimiter): ZFunction1Limiter[A, B] =
      new ZFunction1Limiter[A, B](rateLimiter, function1)

}
