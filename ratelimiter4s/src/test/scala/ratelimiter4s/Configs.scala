package ratelimiter4s

import java.time.Duration

import io.github.resilience4j.ratelimiter.RateLimiterConfig

/**
 * @author Ayush Mittal
 */
trait Configs {

  val onePerMillis: RateLimiterConfig = RateLimiterConfig
    .custom()
    .timeoutDuration(Duration.ofMillis(100))
    .limitRefreshPeriod(Duration.ofSeconds(1))
    .limitForPeriod(1)
    .build()



  val twoPerMillis: RateLimiterConfig = RateLimiterConfig
    .custom()
    .timeoutDuration(Duration.ofMillis(100))
    .limitRefreshPeriod(Duration.ofSeconds(1))
    .limitForPeriod(2)
    .build()

}
