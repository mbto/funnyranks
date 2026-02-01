package com.github.mbto.funnyranks.broker;

import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.broker.handlers.MessageHandler;
import com.github.mbto.funnyranks.service.MaxMindDbService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.Set;

@Service
@Lazy(false)
@Slf4j
public class Initializer {
    @Autowired
    private Map<UInteger, MessageHandler> messageHandlerByAppId;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private Distributor distributor;
    @Autowired
    private SessionsSender sessionsSender;
    @Autowired
    private MaxMindDbService maxMindDbService;

    @PostConstruct
    public void init() throws Throwable {
        if (log.isDebugEnabled())
            log.debug("init() start");

        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(MessageHandler.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(MessageHandler.class.getPackageName());
        for (BeanDefinition component : components) {
            Class<?> aClass = Class.forName(component.getBeanClassName());
            MessageHandler messageHandler = (MessageHandler) aClass.getConstructor().newInstance();
            messageHandlerByAppId.put(messageHandler.getAppId(), messageHandler);
            // caching components, without applicationContext lookups in com.github.mbto.funnyranks.MessagesConsumer
            messageHandler.setPlayersViewByPort(playersViewByPort);
            messageHandler.setSessionsSender(sessionsSender);
            messageHandler.setMaxMindDbService(maxMindDbService);
        }

        distributor.applyChanges(null);
        distributor.launchDistributorAsync();

        if (log.isDebugEnabled())
            log.debug("init() finish");
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        log.info("ApplicationContext is closing — shutting down taskScheduler");
        taskScheduler.initiateShutdown();
    }
}