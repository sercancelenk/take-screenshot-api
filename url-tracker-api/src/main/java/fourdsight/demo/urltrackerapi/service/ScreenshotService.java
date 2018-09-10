package fourdsight.demo.urltrackerapi.service;

import fourdsight.demo.urltrackerapi.config.RabbitMqConfiguration;
import fourdsight.demo.urltrackerapi.model.UrlNameRequest;
import fourdsight.demo.urltrackerapi.model.UrlProcessRequest;
import fourdsight.demo.urltrackerapi.util.LoggerSupport;
import fourdsight.demo.urltrackerapi.util.ScreenShotTakeHelper;
import fourdsight.demo.urltrackerapi.util.cache.HazelcastCacheMapManager;
import fourdsight.demo.urltrackerapi.util.cache.impl.HazelcastCacheStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author Sercan CELENK created by IntelliJ
 */

@Service
public class ScreenshotService implements LoggerSupport {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(HazelcastCacheMapManager.HazelcastCacheNames.DEFAULT_MAP_FOUR_HOURS_TTL.name)
    private HazelcastCacheStore hazelcastCacheStore;

    public String takeScreenshot(UrlNameRequest urlNameRequest) {
        getLogger().info("Processing single screenshot. [url={}]", urlNameRequest.getUrl());

        UrlProcessRequest urlProcessRequest = new UrlProcessRequest(UUID.randomUUID().toString(), urlNameRequest.getUrl());

        String screenshot = ScreenShotTakeHelper.getScreenShot(urlNameRequest.getUrl(), "screenshot_" + urlProcessRequest.getThreadName());

        return screenshot;
    }

    public String takeScreenshotMultiple(List<String> urls) {
        String threadId = UUID.randomUUID().toString();

        for (String url : urls) {
            UrlProcessRequest request = new UrlProcessRequest(threadId, url);
            rabbitTemplate.convertAndSend(RabbitMqConfiguration.QUEUE_URL, request);
        }

        return threadId;
    }

    public List<String> getScreenshots(String threadId) {
        Cache.ValueWrapper valueWrapper = hazelcastCacheStore.get(threadId);
        return Objects.nonNull(valueWrapper) ? (List<String>) valueWrapper.get() : new ArrayList<>();
    }

}
