package pl.jozwik.fastcache

import com.github.blemale.scaffeine.{ Cache, Scaffeine }

import scala.concurrent.duration.FiniteDuration

object FastCache {

  def expireAfterWrite[K, V](duration: FiniteDuration): FastCache[K, V] = {
    val stale = createExpireAfterWrite(duration).build[K, V]()
    val cache = createExpireAfterWrite(duration)
      .evictionListener { (k: K, v: V, _) =>
        stale.put(k, v)
      }
      .build[K, V]()
    new FastCache(cache, stale)
  }

  private def createExpireAfterWrite(duration: FiniteDuration): Scaffeine[Any, Any] =
    Scaffeine().expireAfterWrite(duration)

}

class FastCache[K, V](underlying: Cache[K, V], stale: Cache[K, V]) {
  underlying
}
