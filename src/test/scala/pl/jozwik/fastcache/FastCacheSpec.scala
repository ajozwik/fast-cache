package pl.jozwik.fastcache

import com.github.blemale.scaffeine.Scaffeine
import scala.concurrent.duration.*

class FastCacheSpec extends AbstractSpec {

  "Fast cache " should {
    "Clone" in {
      val cache    = FastCache.expireAfterWrite[Int, Long](2.minute)
      val newCache = cache

    }
  }

}
