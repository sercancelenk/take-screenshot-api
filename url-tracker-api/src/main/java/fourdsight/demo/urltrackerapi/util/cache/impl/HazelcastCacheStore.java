package fourdsight.demo.urltrackerapi.util.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import fourdsight.demo.urltrackerapi.util.cache.CacheStore;
import fourdsight.demo.urltrackerapi.util.cache.HazelcastCacheMapManager;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public class HazelcastCacheStore<K, V> implements CacheStore<K, V>, LoggerSupport {

    private static final DataSerializable NULL = new HazelcastCacheStore.NullDataSerializable();
    private HazelcastInstance hazelcastInstance;
    private HazelcastCacheMapManager.MapInfo mapInfo;
    private IMap<K, V> bucketMap;
    private ReplicatedMap<K, V> replicatedMap;
    private String mapName;

    public HazelcastCacheStore(HazelcastInstance hazelcastInstance, String mapName) {
        this.hazelcastInstance = hazelcastInstance;
        this.mapInfo = HazelcastCacheMapManager.getMapInfo(mapName);
        this.mapName = mapName;
        getLogger().info("[Hazelcast Cache Store] initialized for map {}]", mapInfo);
    }

    @Override
    public String getName() {
        return this.mapName;
    }

    @Override
    public Object getNativeCache() {
        if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED)) return getReplicatedMap();
        return getMap();
    }

    @Override
    public ValueWrapper get(Object key) {
        if (key == null) {
            return null;
        } else {
            Object value = this.lookup(key);
            return value != null ? new SimpleValueWrapper(this.fromStoreValue(value)) : null;
        }
    }

    @Override
    public <V> V get(Object key, Class<V> type) {
        Object value = this.fromStoreValue(this.lookup(key));
        if (type != null && value != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        } else {
            return (V) value;
        }
    }

    @Override
    public <V> V get(Object key, Callable<V> valueLoader) {
        Object value = this.lookup(key);
        if (value != null) {
            return (V) this.fromStoreValue(value);
        } else {
            if (!mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED)) {
                getMap().lock((K) key);

                Object var4;
                try {
                    value = this.lookup(key);
                    if (value == null) {
                        var4 = this.loadValue(key, valueLoader);
                        return (V) var4;
                    }

                    var4 = this.fromStoreValue(value);
                } finally {
                    getMap().unlock((K) key);
                }

                return (V) var4;
            }

        }
        return (V) value;
    }

    private <V> V loadValue(Object key, Callable<V> valueLoader) {
        V value;
        try {
            value = valueLoader.call();
        } catch (Exception var5) {
            throw HazelcastCacheStore.ValueRetrievalExceptionResolver.resolveException(key, valueLoader, var5);
        }

        this.put(key, value);
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        if (key != null) {
            if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED)) {
                if (mapInfo.getTimeToLiveSeconds() < 0) {
                    getReplicatedMap().put((K) key, (V) this.toStoreValue(value));
                } else
                    getReplicatedMap().put((K) key, (V) this.toStoreValue(value), mapInfo.getTimeToLiveSeconds(), mapInfo.getTimeUnit());
            } else {
                getMap().put((K) key, (V) value);
            }
        }

    }

    @Override
    public void put(Object key, Object value, int timeToLiveSeconds) {
        if (key != null) {
            if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED)) {
                getReplicatedMap().put((K) key, (V) this.toStoreValue(value), timeToLiveSeconds, mapInfo.getTimeUnit());
            } else {
                getMap().put((K) key, (V) value, timeToLiveSeconds, TimeUnit.SECONDS);
            }
        }

    }

    protected Object toStoreValue(Object value) {
        return value == null ? NULL : value;
    }

    protected Object fromStoreValue(Object value) {
        return NULL.equals(value) ? null : value;
    }

    @Override
    public void evict(Object key) {
        if (key != null) {
            if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED))
                getReplicatedMap().remove(key);
            else
                getMap().delete(key);
        }
    }

    @Override
    public void clear() {
        if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED))
            getReplicatedMap().clear();
        else
            getMap().clear();
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED)) {
            Object result = getReplicatedMap().putIfAbsent((K) key, (V) this.toStoreValue(value));
            return result != null ? new SimpleValueWrapper(this.fromStoreValue(result)) : null;
        } else {
            Object result = getMap().putIfAbsent((K) key, (V) this.toStoreValue(value));
            return result != null ? new SimpleValueWrapper(this.fromStoreValue(result)) : null;
        }
    }

    private Object lookup(Object key) {
        if (mapInfo.getMapType().equals(HazelcastCacheMapManager.MapType.REPLICATED))
            return getReplicatedMap().get(key);
        return getMap().get(key);
    }

    public IMap<K, V> getMap() {
        if (Objects.isNull(bucketMap)) bucketMap = hazelcastInstance.getMap(mapName);
        return bucketMap;
    }

    public ReplicatedMap<K, V> getReplicatedMap() {
        if (Objects.isNull(replicatedMap)) replicatedMap = hazelcastInstance.getReplicatedMap(mapName);
        return replicatedMap;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    private static class ValueRetrievalExceptionResolver {
        private ValueRetrievalExceptionResolver() {
        }

        static RuntimeException resolveException(Object key, Callable<?> valueLoader, Throwable ex) {
            return new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    static final class NullDataSerializable implements DataSerializable {
        NullDataSerializable() {
        }

        public void writeData(ObjectDataOutput out) throws IOException {
        }

        public void readData(ObjectDataInput in) throws IOException {
        }

        public boolean equals(Object obj) {
            return obj != null && obj.getClass() == this.getClass();
        }

        public int hashCode() {
            return 0;
        }
    }
}