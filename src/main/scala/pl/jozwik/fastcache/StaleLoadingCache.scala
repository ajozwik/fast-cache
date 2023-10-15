package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.LoadingCache

import java.util.Objects
import java.{ lang, util }
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
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
    val s        = staleCache.getAll(iterable)
    val c        = cache.getAll(iterable)
    val combined = util.stream.Stream.concat(s.entrySet.stream, c.entrySet.stream)
    combined.collect(Collectors.toUnmodifiableMap((e: util.Map.Entry[K, V]) => e.getKey, e => e.getValue))
  }

  override def refresh(k: K): CompletableFuture[V] =
    cache.refresh(k)

  override def refreshAll(iterable: lang.Iterable[? <: K]): CompletableFuture[util.Map[K, V]] =
    cache.refreshAll(iterable)

}
