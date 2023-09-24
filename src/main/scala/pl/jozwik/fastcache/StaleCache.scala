package pl.jozwik.fastcache

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.ExecutionContext

class StaleCache[K, V](private[fastcache] val cache: Cache[K, V], private[fastcache] val staleCache: Cache[K, V])(implicit ex: ExecutionContext)
  extends AbstractStaleCache(cache, staleCache)
