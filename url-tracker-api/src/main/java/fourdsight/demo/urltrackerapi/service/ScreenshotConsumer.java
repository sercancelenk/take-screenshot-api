package fourdsight.demo.urltrackerapi.service;

import fourdsight.demo.urltrackerapi.config.RabbitMqConfiguration;
import fourdsight.demo.urltrackerapi.model.UrlProcessRequest;
import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import fourdsight.demo.urltrackerapi.util.ScreenShotTakeHelper;
import fourdsight.demo.urltrackerapi.util.cache.HazelcastCacheMapManager;
import fourdsight.demo.urltrackerapi.util.cache.impl.HazelcastCacheStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Sercan CELENK created by IntelliJ
 */

@Component
public class ScreenshotConsumer implements LoggerSupport {

    @Autowired
    @Qualifier(HazelcastCacheMapManager.HazelcastCacheNames.DEFAULT_MAP_FOUR_HOURS_TTL.name)
    private HazelcastCacheStore hazelcastCacheStore;

    @RabbitListener(queues = RabbitMqConfiguration.QUEUE_URL)
    public void process(UrlProcessRequest urlProcessRequest){
        getLogger().info("Processing Single Screenshot Consumer. Url={} ThreadID={}", urlProcessRequest.getUrl(), urlProcessRequest.getThreadName());

        String screenshot = ScreenShotTakeHelper.getScreenShot(urlProcessRequest.getUrl(), "screenshot_" + urlProcessRequest.getThreadName() + "_" + new Random(10000).nextInt());

        if (StringUtils.isNoneBlank(screenshot))
            addToCache(urlProcessRequest, screenshot);
    }

    private void addToCache(UrlProcessRequest urlProcessRequests, String screenshot) {
        Cache.ValueWrapper valueWrapper = hazelcastCacheStore.get(urlProcessRequests.getThreadName());
        List<String> screenshotsCache = new ArrayList<>();
        if (Objects.nonNull(valueWrapper)) {
            screenshotsCache = (List<String>) hazelcastCacheStore.get(urlProcessRequests.getThreadName()).get();
        }
        if (CollectionUtils.isEmpty(screenshotsCache)) {
            List<String> list = new ArrayList<>();
            list.add(screenshot);
            hazelcastCacheStore.put(urlProcessRequests.getThreadName(), list);
        } else {
            screenshotsCache.add(screenshot);
            hazelcastCacheStore.put(urlProcessRequests.getThreadName(), screenshotsCache);
        }
    }
}
