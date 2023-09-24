package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.benmanes.caffeine.cache.{ Cache, Policy }

import java.{ lang, util }
import java.util.concurrent.ConcurrentMap
import java.util.stream.{ Collectors, StreamSupport }
import java.util.{ function, stream, Collections, Objects }
import scala.concurrent.ExecutionContext

class AbstractStaleCache[K, V](cache: Cache[K, V], staleCache: Cache[K, V])(implicit ex: ExecutionContext) extends Cache[K, V] {

  override def getIfPresent(k: K): V = {
    val cv = cache.getIfPresent(k)
    if (Objects.isNull(cv)) {
      staleCache.getIfPresent(k)
    } else {
      cv
    }
  }

  override def get(k: K, f: function.Function[? >: K, ? <: V]): V = {
    val cv = cache.getIfPresent(k)
    if (Objects.isNull(cv)) {
      val sv = staleCache.getIfPresent(k)
      if (Objects.isNull(sv)) {
        cache.get(k, f)
      } else {
        ex.execute(() => cache.get(k, f))
        sv
      }
    } else {
      cv
    }
  }

  override def getAllPresent(iterable: lang.Iterable[? <: K]): util.Map[K, V] = {
    val sm = staleCache.getAllPresent(iterable)
    val cm = cache.getAllPresent(iterable)
    sm.putAll(cm)
    sm
  }

  override def getAll(iterable: lang.Iterable[? <: K], f: function.Function[? >: util.Set[? <: K], ? <: util.Map[? <: K, ? <: V]]): util.Map[K, V] = {
    val sc          = getAllPresent(iterable)
    val set         = sc.keySet()
    val missingKeys = StreamSupport.stream(iterable.spliterator(), false).filter(el => !set.contains(el)).collect(Collectors.toSet[K])
    val missing     = cache.getAll(missingKeys, f)
    sc.putAll(missing)
    sc
  }

  override def put(k: K, v: V): Unit = cache.put(k, v)

  override def putAll(map: util.Map[? <: K, ? <: V]): Unit = cache.putAll(map)

  override def invalidate(k: K): Unit = cache.invalidate(k)

  override def invalidateAll(iterable: lang.Iterable[? <: K]): Unit = cache.invalidateAll(iterable)

  override def invalidateAll(): Unit = cache.invalidateAll()

  override def estimatedSize(): Long = cache.estimatedSize()

  override def stats(): CacheStats = cache.stats()

  override def asMap(): ConcurrentMap[K, V] = {
    val m = staleCache.asMap()
    m.putAll(cache.asMap())
    m
  }

  override def cleanUp(): Unit = cache.cleanUp()

  override def policy(): Policy[K, V] = cache.policy()

  protected def sum(maps: util.Map[K, V]*): util.Map[K, V] = {
    def keyMap(e: util.Map.Entry[K, V])   = e.getKey
    def valueMap(e: util.Map.Entry[K, V]) = e.getValue
    val map =
      stream.Stream
        .of(maps*)
        .map(x => x.entrySet)
        .flatMap(_.stream)
        .collect(Collectors.toMap(keyMap, valueMap));
    Collections.unmodifiableMap(map)
  }

}
