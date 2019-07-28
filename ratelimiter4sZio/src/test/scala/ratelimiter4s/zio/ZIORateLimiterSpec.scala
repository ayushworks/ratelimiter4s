package ratelimiter4s.zio

import io.github.resilience4j.ratelimiter.RateLimiter
import org.scalatest.{ EitherValues, Matchers, WordSpec }
import ratelimiter4s.Configs
import ratelimiter4s.zio.ZIORateLimiter._
import zio.{ DefaultRuntime, FiberFailure }

/**
 * @author Ayush Mittal
 */
class ZIORateLimiterSpec extends WordSpec with Matchers with EitherValues with Configs {

  val runtime = new DefaultRuntime {}

  "Rate limiter" when {

    "Function0 is rate limitedd" should {

      def service: String = "value"

      "return RequestNotPermitted when rate limit is breached" in {

        val onePerMillisLimiter =
          RateLimiter.of("onePerMillisLimiter", onePerMillis)

        val rateLimitedService: ZFunction0Limiter[String] = limit(service _, onePerMillisLimiter)

        val prog = for {
          _       <- rateLimitedService.pure
          result2 <- rateLimitedService.pure
        } yield result2

        val result = runtime.unsafeRun(prog.either)
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerMillisLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerMillisLimiter)

        val prog = for {
          _       <- rateLimitedService.pure
          result2 <- rateLimitedService.pure
        } yield result2

        val result = runtime.unsafeRun(prog.either)
        result.isRight shouldBe true
        result.right.value shouldBe "value"
      }

      "throw exception if thrown from service" in {

        def service: String = throw new RuntimeException("error")

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure
          result2 <- rateLimitedService.pure
        } yield result2

        intercept[FiberFailure](runtime.unsafeRun(prog.either))

      }

    }

    "Function1 is rate limitedd" should {

      def service(name: String): String =
        s"hello $name"

      "return RequestNotPermitted when rate limit is breached" in {

        val onePerSecondLimiter =
          RateLimiter.of("onePerSecondLimiter", onePerMillis)

        val rateLimitedService = limit(service _, onePerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure("John")
          result2 <- rateLimitedService.pure("Bob")
        } yield result2

        val result = runtime.unsafeRun(prog.either)
        result.isLeft shouldBe true
        result.left.value.getMessage shouldBe "RateLimiter 'onePerSecondLimiter' does not permit further calls"
      }

      "return value when rate limit is not breached" in {

        val twoPerMillisLimiter =
          RateLimiter.of("twoPerMillisLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerMillisLimiter)

        val prog = for {
          _       <- rateLimitedService.pure("John")
          result2 <- rateLimitedService.pure("Bob")
        } yield result2

        val result = runtime.unsafeRun(prog.either)
        result.isRight shouldBe true
        result.right.value shouldBe "hello Bob"

      }

      "throw exception if thrown from service" in {

        def service(name: String): String = throw new RuntimeException("error")

        val twoPerSecondLimiter = RateLimiter.of("twoPerSecondLimiter", twoPerMillis)

        val rateLimitedService = limit(service _, twoPerSecondLimiter)

        val prog = for {
          _       <- rateLimitedService.pure("John")
          result2 <- rateLimitedService.pure("Bob")
        } yield result2

        intercept[FiberFailure](runtime.unsafeRun(prog.either))
      }

    }

  }
}
