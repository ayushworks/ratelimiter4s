package ratelimiter4s.cats

import io.github.resilience4j.ratelimiter.RateLimiter
import org.scalatest.{ EitherValues, Matchers, WordSpec }
import ratelimiter4s.Configs
import ratelimiter4s.cats.CatsRateLimiter._

/**
 * @author Ayush Mittal
 */
class CatsRateLimiterUnsafeRunSpec extends WordSpec with Matchers with EitherValues with Configs {

  "Rate limiter" when {

    "Function1 is decorated" should {

      def service(name: String): String = {
        if (name.exists(_.isDigit)) {
          throw new RuntimeException("error")
        }
        s"hello $name"
      }

      "return RequestNotPermitted when rate limit is breached" in {

        implicit val onePerSecondLimiter =
          RateLimiter.of("onePerSecondLimiter", onePerMillis)

        val rateLimitedService = (service _).decorate

        val prog = for {
          _       <- rateLimitedService("John")
          result2 <- rateLimitedService("Bob")
        } yield result2

        val result = prog.value.unsafeRunSync()
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        implicit val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = (service _).decorate

        val prog = for {
          _       <- rateLimitedService("John")
          result2 <- rateLimitedService("Bob")
        } yield result2

        val result = prog.value.unsafeRunSync()
        result.isRight shouldBe true
        result.right.value shouldBe "hello Bob"

      }

      "return exception when it is thrown" in {

        implicit val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = (service _).decorate

        val prog = for {
          _       <- rateLimitedService("1")
          result2 <- rateLimitedService("Bob")
        } yield result2

        val result = prog.value.unsafeRunSync()
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "error"
      }
    }

  }
}
