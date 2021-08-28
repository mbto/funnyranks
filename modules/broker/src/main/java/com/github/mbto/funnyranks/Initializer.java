package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.handlers.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private Distributor distributor;
    @Autowired
    private SessionsSender sessionsSender;

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
        }

        distributor.applyChanges(null, true);
        distributor.launchDistributorAsync();

        if (log.isDebugEnabled())
            log.debug("init() finish");
    }
}