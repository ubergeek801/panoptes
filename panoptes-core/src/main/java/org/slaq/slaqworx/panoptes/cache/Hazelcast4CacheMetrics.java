/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.slaq.slaqworx.panoptes.cache;

import java.util.concurrent.TimeUnit;

import com.hazelcast.map.IMap;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import io.micrometer.core.lang.Nullable;

/**
 * This class is copied from the original Micrometer source, trivially adapted to Hazelcast 4.
 * <p>
 * Collect metrics on Hazelcast caches, including detailed metrics on storage space, near cache
 * usage, and timings.
 *
 * @author Jon Schneider
 */
@NonNullApi
@NonNullFields
public class Hazelcast4CacheMetrics extends CacheMeterBinder {
    /**
     * Record metrics on a Hazelcast cache.
     *
     * @param registry
     *            The registry to bind metrics to.
     * @param cache
     *            The cache to instrument.
     * @param tags
     *            Tags to apply to all recorded metrics.
     * @param <C>
     *            The cache type.
     * @param <K>
     *            The cache key type.
     * @param <V>
     *            The cache value type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in
     *         any way.
     */
    public static <K, V, C extends IMap<K, V>> C monitor(MeterRegistry registry, C cache,
            Iterable<Tag> tags) {
        new Hazelcast4CacheMetrics(cache, tags).bindTo(registry);
        return cache;
    }

    /**
     * Record metrics on a Hazelcast cache.
     *
     * @param registry
     *            The registry to bind metrics to.
     * @param cache
     *            The cache to instrument.
     * @param tags
     *            Tags to apply to all recorded metrics. Must be an even number of arguments
     *            representing key/value pairs of tags.
     * @param <C>
     *            The cache type.
     * @param <K>
     *            The cache key type.
     * @param <V>
     *            The cache value type.
     * @return The instrumented cache, unchanged. The original cache is not wrapped or proxied in
     *         any way.
     */
    public static <K, V, C extends IMap<K, V>> C monitor(MeterRegistry registry, C cache,
            String... tags) {
        return monitor(registry, cache, Tags.of(tags));
    }

    private final IMap<?, ?> cache;

    public <K, V, C extends IMap<K, V>> Hazelcast4CacheMetrics(C cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
        this.cache = cache;
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        Gauge.builder("cache.entries", cache,
                cache -> cache.getLocalMapStats().getBackupEntryCount())
                .tags(getTagsWithCacheName()).tag("ownership", "backup")
                .description("The number of backup entries held by this member").register(registry);

        Gauge.builder("cache.entries", cache,
                cache -> cache.getLocalMapStats().getOwnedEntryCount()).tags(getTagsWithCacheName())
                .tag("ownership", "owned")
                .description("The number of owned entries held by this member").register(registry);

        Gauge.builder("cache.entry.memory", cache,
                cache -> cache.getLocalMapStats().getBackupEntryMemoryCost())
                .tags(getTagsWithCacheName()).tag("ownership", "backup")
                .description("Memory cost of backup entries held by this member")
                .baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("cache.entry.memory", cache,
                cache -> cache.getLocalMapStats().getOwnedEntryMemoryCost())
                .tags(getTagsWithCacheName()).tag("ownership", "owned")
                .description("Memory cost of owned entries held by this member")
                .baseUnit(BaseUnits.BYTES).register(registry);

        FunctionCounter
                .builder("cache.partition.gets", cache,
                        cache -> cache.getLocalMapStats().getGetOperationCount())
                .tags(getTagsWithCacheName())
                .description("The total number of get operations executed against this partition")
                .register(registry);

        timings(registry);
        nearCacheMetrics(registry);
    }

    @Nullable
    @Override
    protected Long evictionCount() {
        return null;
    }

    /**
     * @return The number of hits against cache entries held in this local partition. Not all gets
     *         had to result from a get operation against {@link #cache}. If a get operation
     *         elsewhere in the cluster caused a lookup against an entry held in this partition, the
     *         hit will be recorded against map stats in this partition and not in the map stats of
     *         the calling {@link IMap}.
     */
    @Override
    protected long hitCount() {
        return cache.getLocalMapStats().getHits();
    }

    /**
     * @return There is no way to calculate miss count in Hazelcast. See issue #586.
     */
    @Override
    protected Long missCount() {
        return null;
    }

    private void nearCacheMetrics(MeterRegistry registry) {
        if (cache.getLocalMapStats().getNearCacheStats() != null) {
            Gauge.builder("cache.near.requests", cache,
                    cache -> cache.getLocalMapStats().getNearCacheStats().getHits())
                    .tags(getTagsWithCacheName()).tag("result", "hit")
                    .description(
                            "The number of hits (reads) of near cache entries owned by this member")
                    .register(registry);

            Gauge.builder("cache.near.requests", cache,
                    cache -> cache.getLocalMapStats().getNearCacheStats().getMisses())
                    .tags(getTagsWithCacheName()).tag("result", "miss")
                    .description(
                            "The number of hits (reads) of near cache entries owned by this member")
                    .register(registry);

            Gauge.builder("cache.near.evictions", cache,
                    cache -> cache.getLocalMapStats().getNearCacheStats().getEvictions())
                    .tags(getTagsWithCacheName())
                    .description(
                            "The number of evictions of near cache entries owned by this member")
                    .register(registry);

            Gauge.builder("cache.near.persistences", cache,
                    cache -> cache.getLocalMapStats().getNearCacheStats().getPersistenceCount())
                    .tags(getTagsWithCacheName())
                    .description(
                            "The number of Near Cache key persistences (when the pre-load feature is enabled)")
                    .register(registry);
        }
    }

    @Override
    protected long putCount() {
        return cache.getLocalMapStats().getPutOperationCount();
    }

    @Override
    protected Long size() {
        return cache.getLocalMapStats().getOwnedEntryCount();
    }

    private void timings(MeterRegistry registry) {
        FunctionTimer.builder("cache.gets.latency", cache,
                cache -> cache.getLocalMapStats().getGetOperationCount(),
                cache -> cache.getLocalMapStats().getTotalGetLatency(), TimeUnit.MILLISECONDS)
                .tags(getTagsWithCacheName()).description("Cache gets").register(registry);

        FunctionTimer.builder("cache.puts.latency", cache,
                cache -> cache.getLocalMapStats().getPutOperationCount(),
                cache -> cache.getLocalMapStats().getTotalPutLatency(), TimeUnit.MILLISECONDS)
                .tags(getTagsWithCacheName()).description("Cache puts").register(registry);

        FunctionTimer.builder("cache.removals.latency", cache,
                cache -> cache.getLocalMapStats().getRemoveOperationCount(),
                cache -> cache.getLocalMapStats().getTotalRemoveLatency(), TimeUnit.MILLISECONDS)
                .tags(getTagsWithCacheName()).description("Cache removals").register(registry);
    }
}
