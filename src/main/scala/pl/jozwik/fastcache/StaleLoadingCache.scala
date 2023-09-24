package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.LoadingCache

import java.util.Objects
import java.{ lang, util }
import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext

class StaleLoadingCache[K, V](cache: LoadingCache[K, V], staleCache: LoadingCache[K, V])(implicit ex: ExecutionContext)
  extends AbstractStaleCache[K, V](cache, staleCache)
  with LoadingCache[K, V] {

  override def get(k: K): V = {
    val cv = cache.getIfPresent(k)
    if (Objects.isNull(cv)) {
      val sc = staleCache.getIfPresent(k)
      ex.execute(() => cache.get(k))
      sc
    } else {
      cv
    }
  }

  override def getAll(iterable: lang.Iterable[? <: K]): util.Map[K, V] = {
    ???
  }

  override def refresh(k: K): CompletableFuture[V] =
    cache.refresh(k)

  override def refreshAll(iterable: lang.Iterable[? <: K]): CompletableFuture[util.Map[K, V]] =
    cache.refreshAll(iterable)

}
