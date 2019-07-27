package ratelimiter4s.core

import io.github.resilience4j.ratelimiter.{RateLimiter, RequestNotPermitted}
import org.scalatest.{EitherValues, Matchers, WordSpec}
import ratelimiter4s.Configs
import ratelimiter4s.core.FRateLimiter._

/**
 * @author Ayush Mittal
 */
class FRateLimiterSpec extends WordSpec with Matchers with EitherValues with Configs {

  "Rate limiter" when {

    "Function0 is decorated" should {

      def service: String = "value"

      "return RequestNotPermitted when rate limit is breached" in {

        implicit val onePerMilliSecondLimiter = RateLimiter.of("onePerMilliSecondLimiter", onePerMillis)

        def rateLimitedService: () => Either[Throwable, String] = (service _).decorate

        rateLimitedService.apply() shouldBe Right("value")
        rateLimitedService.apply().left.value.getMessage shouldBe "RateLimiter 'onePerMilliSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        implicit val twoPerMilliSecondLimiter = RateLimiter.of("twoPerMilliSecondLimiter", twoPerMillis)

        val rateLimitedService = (service _).decorate

        rateLimitedService.pure shouldBe Right("value")
        rateLimitedService.pure shouldBe Right("value")

      }

      "throw exception if thrown from service" in {

        def service: String = throw new RuntimeException("error")

        implicit val onePerMilliSecondLimiter = RateLimiter.of("onePerMilliSecondLimiter", onePerMillis)

        val rateLimitedService = (service _).decorate

        val exception = intercept[RuntimeException] {
          rateLimitedService.pure
        }

        exception.getMessage shouldBe "error"
      }

    }

    "Function1 is decorated" should {

      def service(name: String): String =
        s"Hello $name"

      "return RequestNotPermitted when rate limit is breached" in {

        implicit val onePerMilliSecondLimiter = RateLimiter.of("onePerMilliSecondLimiter", onePerMillis)

        val rateLimitedService = (service _).decorate

        rateLimitedService.pure("John") shouldBe Right("Hello John")
        rateLimitedService.pure("Bob").left.value.getMessage shouldBe "RateLimiter 'onePerMilliSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        implicit val twoPerMilliSecondLimiter = RateLimiter.of("twoPerMilliSecondLimiter", twoPerMillis)

        val rateLimitedService = (service _).decorate


        rateLimitedService.pure("John") shouldBe Right("Hello John")
        rateLimitedService.pure("Bob")  shouldBe Right("Hello Bob")

      }

      "throw exception if thrown from service" in {

        def service(name: String): String = throw new RuntimeException("error")

        implicit val onePerMilliSecondLimiter = RateLimiter.of("onePerMilliSecondLimiter", onePerMillis)

        val rateLimitedService = (service _).decorate

        val exception = intercept[RuntimeException] {
          rateLimitedService.pure("John")
        }

        exception.getMessage shouldBe "error"
      }

    }

  }
}
