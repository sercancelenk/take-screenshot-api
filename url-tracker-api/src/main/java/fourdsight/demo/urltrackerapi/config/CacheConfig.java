package fourdsight.demo.urltrackerapi.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.merge.PutIfAbsentMapMergePolicy;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import fourdsight.demo.urltrackerapi.util.cache.HazelcastCacheMapManager;
import fourdsight.demo.urltrackerapi.util.cache.impl.HazelcastCacheStore;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 * @author Sercan CELENK created by IntelliJ
 */

@Configuration
@EnableCaching
public class CacheConfig implements LoggerSupport {

    @PostConstruct
    public void postConstruct() {
        getLogger().info("CacheConfig initialize successfully.");
    }

    @Configuration
    public static class HazelcastConfiguration {
        private static Logger logger = LoggerFactory.getLogger(HazelcastConfiguration.class);

        @Value("${cache.hazelcast.multicast.enable:true}")
        private boolean isMulticastEnable;

        @Value("${cache.hazelcast.multicast.group:230.0.0.1}")
        private String multicastGroup;

        @Value("${cache.hazelcast.multicast.port:46666}")
        private int multicastPort;

        public HazelcastConfiguration() {
            HazelcastCacheMapManager.prepareHazelcastMapInstancesTypes();
            logger.info("[Common] Hazelcast Cache Config initialize successfully.");
        }

        private static void addMapToConfig(Config config,
                                           String mapName,
                                           int backupCount,
                                           InMemoryFormat inMemoryFormat,
                                           EvictionPolicy evictionPolicy,
                                           int maxIdleSeconds,
                                           boolean readBackupData) {
            MapConfig mapConfig = new MapConfig();
            mapConfig.setName(mapName);
            mapConfig.setBackupCount(backupCount);
            mapConfig.setInMemoryFormat(inMemoryFormat);
            mapConfig.setEvictionPolicy(evictionPolicy);
            mapConfig.setReadBackupData(readBackupData);
            mapConfig.setMaxIdleSeconds(maxIdleSeconds);
            mapConfig.setMaxSizeConfig(new MaxSizeConfig(MaxSizeConfig.DEFAULT_MAX_SIZE, MaxSizeConfig.MaxSizePolicy.PER_NODE));
            mapConfig.setMergePolicy(PutIfAbsentMapMergePolicy.class.getName());


            if (HazelcastCacheMapManager.getMapInfo(mapName).getTimeToLiveSeconds() > 0)
                mapConfig.setTimeToLiveSeconds(HazelcastCacheMapManager.getMapInfo(mapName).getTimeToLiveSeconds());

            config.addMapConfig(mapConfig);

            logger.info("[Hazelcast] Added {} DISTRIBUTED map to default instance. [MapConfig={}]", mapName, mapConfig);
        }

        private Config getBaseConfig(boolean addDefaultMap) {
            Config config = new Config();
            config.setProperty("hazelcast.logging.type", "slf4j");
            if (BooleanUtils.isTrue(addDefaultMap))
                addMapToConfig(config, HazelcastCacheMapManager.HazelcastCacheNames.DEFAULT_MAP_FOUR_HOURS_TTL.name, 1, InMemoryFormat.OBJECT, EvictionPolicy.LRU, HazelcastCacheMapManager.TimeUnits.FIVE_MINUTES_TTL, false);
            return config;
        }

        @Bean(value = "hzDefaultInstance", destroyMethod = "shutdown")
        public HazelcastInstance getDefaultInstance() {

            Config config = getBaseConfig(Boolean.FALSE);
            config.getNetworkConfig().setPortAutoIncrement(true);

            if (isMulticastEnable) {
                config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
                config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);

                config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(isMulticastEnable);
                config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup(multicastGroup);
                config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(multicastPort);
                config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastTimeToLive(1); // same subnet
            }

            config.setInstanceName("[Magazine Service] Distrubuted Hazelcast Instance");

            HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

            return instance;
        }


        @Bean(name = HazelcastCacheMapManager.HazelcastCacheNames.DEFAULT_MAP_FOUR_HOURS_TTL.name)
        public HazelcastCacheStore getHzDefaultMapCacheStoreTenMinutesTTL() {
            return new HazelcastCacheStore<String, Object>(getDefaultInstance(), HazelcastCacheMapManager.HazelcastCacheNames.DEFAULT_MAP_FOUR_HOURS_TTL.name);
        }


        @Primary
        @Bean(HazelcastCacheMapManager.HazelcastCacheNames.HZ_COMMON_CACHE_MANAGER.name)
        public CacheManager getHzCacheManager() {
            return new HazelcastCacheManager(getDefaultInstance());
        }

    }

}