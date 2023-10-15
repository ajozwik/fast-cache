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
      val v       = 4
      cache.getIfPresent(v) shouldBe empty
      cache.get(v, identity) shouldBe v
      cache.getIfPresent(v) shouldBe Option(v)
    }
    "Return from stale cache" in {
      val builder = Scaffeine().expireAfterWrite(1.minute)
      val cache   = FastCache.createStaleCache[String, String](builder)
      val k       = Random.nextPrintableChar().toString
      val v       = Random.nextPrintableChar().toString
      cache.get(k, _ => v) shouldBe v
      val underlyingCache = cache.underlying.asInstanceOf[StaleCache[String, String]]
      logger.debug(s"invalidateAll $k $v")
      underlyingCache.cache.invalidateAll()
      underlyingCache.cache.cleanUp()
      underlyingCache.cache.getIfPresent(k) shouldBe null
      cache.getIfPresent(k) shouldBe Option(v)
      val kk = Random.nextPrintableChar().toString
      cache.get(kk, identity)
      underlyingCache.cache.getIfPresent(kk) shouldBe kk
      underlyingCache.staleCache.getIfPresent(kk) shouldBe kk
      logger.debug(s"invalidate $kk")
      underlyingCache.cache.invalidate(kk)
      cache.cleanUp()
      underlyingCache.cache.getIfPresent(kk) shouldBe null
      underlyingCache.staleCache.getIfPresent(kk) shouldBe kk
      cache.asMap()
      cache.getIfPresent(kk) shouldBe Option(kk)
      cache.getAllPresent(Iterable.empty) shouldBe empty
      cache.getAll(Iterable.empty, keys => keys.map(k => k -> k).toMap) shouldBe empty

    }
  }

}
