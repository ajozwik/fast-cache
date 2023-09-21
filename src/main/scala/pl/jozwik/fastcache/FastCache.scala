package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.blemale.scaffeine.{ Cache, Scaffeine }

import scala.concurrent.ExecutionContext

object FastCache {

  def createStaleCache[K, V](builder: Scaffeine[Any, Any])(implicit ex: ExecutionContext): Cache[K, V] = {
    val staleCache = builder.build[K, V]()
    val removeListener = (k: K, v: V, r: RemovalCause) => {
      staleCache.put(k, v)
    }
    val cache      = builder.removalListener[K, V](removeListener).evictionListener(removeListener).build[K, V]()
    val underlying = new StaleCache[K, V](cache.underlying, staleCache.underlying)
    new Cache(underlying)
  }

}
