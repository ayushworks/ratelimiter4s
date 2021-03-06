package ratelimiter4s.cats

import io.github.resilience4j.ratelimiter.RateLimiter
import org.scalatest.{ EitherValues, Matchers, WordSpec }
import ratelimiter4s.Configs
import ratelimiter4s.cats.CatsRateLimiter._

/**
 * @author Ayush Mittal
 */
class CatsRateLimiterSpec extends WordSpec with Matchers with EitherValues with Configs {

  "Rate limiter" when {

    "Function0 is rate limitedd" should {

      def service: String = "value"

      "return RequestNotPermitted when rate limit is breached" in {

        val onePerSecondLimiter = RateLimiter.of("onePerSecondLimiter", onePerMillis)

        val rateLimitedService: CatsFunction0Limiter[String] = limit(service _, onePerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure
          result2 <- rateLimitedService.pure
        } yield result2

        val result = prog.value.unsafeRunSync
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure
          result2 <- rateLimitedService.pure
        } yield result2

        val result = prog.value.unsafeRunSync
        result.isRight shouldBe true
        result.right.value shouldBe "value"

      }

      "throw exception if thrown from service" in {

        def service: String = throw new RuntimeException("error")

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val exception = intercept[RuntimeException] {
          for {
            _       <- rateLimitedService.pure
            result2 <- rateLimitedService.pure
          } yield result2
        }

        exception.getMessage shouldBe "error"
      }

    }

    "Function1 is rate limitedd" should {

      def service(name: String): String =
        s"hello $name"

      "return RequestNotPermitted when rate limit is breached" in {

        val onePerSecondLimiter = RateLimiter.of("onePerSecondLimiter", onePerMillis)

        val rateLimitedService = limit(service _, onePerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure("John")
          result2 <- rateLimitedService.pure("Bob")
        } yield result2

        val result = prog.value.unsafeRunSync
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure("John")
          result2 <- rateLimitedService.pure("Bob")
        } yield result2

        val result = prog.value.unsafeRunSync
        result.isRight shouldBe true
        result.right.value shouldBe "hello Bob"

      }

      "throw exception if thrown from service" in {

        def service(name: String): String = throw new RuntimeException("error")

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val exception = intercept[RuntimeException] {
          for {
            _       <- rateLimitedService.pure("John")
            result2 <- rateLimitedService.pure("Bob")
          } yield result2
        }

        exception.getMessage shouldBe "error"
      }

    }

  }
}
