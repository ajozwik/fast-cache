package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.blemale.scaffeine.{ Cache, LoadingCache, Scaffeine }
import com.github.benmanes.caffeine.cache.Cache as CCache
import scala.concurrent.ExecutionContext

object FastCache {

  def createStaleCache[K, V](builder: Scaffeine[Any, Any])(implicit ex: ExecutionContext): Cache[K, V] = {
    val staleCache = builder.build[K, V]()
    val removeListener = (k: K, v: V, _: RemovalCause) => {
      staleCache.put(k, v)
    }
    val cache                    = builder.removalListener[K, V](removeListener).evictionListener(removeListener).build[K, V]()
    val underlying: CCache[K, V] = new StaleCache[K, V](cache.underlying, staleCache.underlying)
    new Cache(underlying)
  }

  def createStaleLoadingCache[K, V](builder: Scaffeine[Any, Any], loader: K => V)(implicit ex: ExecutionContext): LoadingCache[K, V] = {
    val staleCache = builder.build[K, V](loader)
    val removeListener = (k: K, v: V, _: RemovalCause) => {
      staleCache.put(k, v)
    }
    val cache      = builder.removalListener[K, V](removeListener).evictionListener(removeListener).build[K, V](loader)
    val underlying = new StaleLoadingCache[K, V](cache.underlying, staleCache.underlying)
    new LoadingCache(underlying)
  }

}
