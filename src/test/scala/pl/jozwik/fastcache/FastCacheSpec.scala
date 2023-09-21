package pl.jozwik.fastcache

import com.github.blemale.scaffeine.Scaffeine

import concurrent.ExecutionContext.Implicits.global
import concurrent.duration.*
import scala.util.Random

class FastCacheSpec extends AbstractSpec {

  "Fast cache " should {
    "Return empty" in {
      val builder = Scaffeine()
      val cache   = FastCache.createStaleCache[Long, Long](builder)
      cache.getIfPresent(4) shouldBe empty
    }
    "Return from stale cache" in {
      val builder = Scaffeine().expireAfterWrite(1.minute)
      val cache   = FastCache.createStaleCache[String, String](builder)
      val k       = Random.nextPrintableChar().toString
      val v       = Random.nextPrintableChar().toString
      cache.put(k, v)
      val underlyingCache = cache.underlying.asInstanceOf[StaleCache[String, String]]
      logger.debug(s"invalidateAll $k $v")
      cache.invalidateAll()
      cache.cleanUp()
      underlyingCache.cache.getIfPresent(k) shouldBe null
      cache.getIfPresent(k) shouldBe Option(v)
      val kk = Random.nextPrintableChar().toString
      cache.get(kk, identity)
      underlyingCache.cache.getIfPresent(kk) shouldBe kk
      underlyingCache.staleCache.getIfPresent(kk) shouldBe null
      logger.debug(s"invalidate $kk")
      cache.invalidate(kk)
      cache.cleanUp()
      underlyingCache.cache.getIfPresent(kk) shouldBe null
      cache.getIfPresent(kk) shouldBe Option(kk)
    }
  }

}
