package ratelimiter4s.core

import io.github.resilience4j.ratelimiter.RateLimiter
import org.scalatest.{ EitherValues, Matchers, WordSpec }
import ratelimiter4s.Configs
import FRateLimiter._

/**
 * @author Ayush Mittal
 */
class FRateLimiterUnsafeRunSpec extends WordSpec with Matchers with EitherValues with Configs {

  "Rate limiter" when {

    "Function1 is rate limitedd" should {

      def service(name: String): String = {
        if (name.exists(_.isDigit)) {
          throw new RuntimeException("error")
        }
        s"hello $name"
      }

      "return RequestNotPermitted when rate limit is breached" in {

        val onePerSecondLimiter =
          RateLimiter.of("onePerSecondLimiter", onePerMillis)

        val rateLimitedService = limit(service _, onePerSecondLimiter)

        val result = for {
          _       <- rateLimitedService("John")
          result2 <- rateLimitedService("Bob")
        } yield result2

        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerMillisLimiter)

        val result = for {
          _       <- rateLimitedService("John")
          result2 <- rateLimitedService("Bob")
        } yield result2

        result.isRight shouldBe true
        result.right.value shouldBe "hello Bob"

      }

      "return exception when it is thrown" in {

        val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerMillisLimiter)

        val result = for {
          _       <- rateLimitedService("1")
          result2 <- rateLimitedService("Bob")
        } yield result2

        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "error"
      }
    }

  }
}
