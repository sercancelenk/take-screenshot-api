package fourdsight.demo.urltrackerapi.util.cache;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author Sercan CELENK created by IntelliJ
 */

public interface CacheStore<K, V> extends Cache {

    @Override
    String getName();

    @Override
    Object getNativeCache();

    @Override
    ValueWrapper get(Object key);

    @Override
    <T> T get(Object key, Class<T> type);

    @Override
    <T> T get(Object key, Callable<T> valueLoader);

    @Override
    void put(Object key, Object value);

    @Override
    ValueWrapper putIfAbsent(Object key, Object value);

    void put(Object key, Object value, int timeToLiveSeconds);

    @Override
    void evict(Object key);

    @Override
    void clear();
}