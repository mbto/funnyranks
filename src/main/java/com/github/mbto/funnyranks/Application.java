package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.Partition;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.handlers.MessageHandler;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication(exclude = {JooqAutoConfiguration.class,
        TaskExecutionAutoConfiguration.class, TaskSchedulingAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class Application implements SchedulingConfigurer {
    static {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public BlockingDeque<Message<?>> defaultPartition() {
        return new LinkedBlockingDeque<>(Integer.MAX_VALUE);
    }

    /**
     * Relationship registry<br/>
     * Key: steam application ID<br/>
     * Value: MessageHandler
     */
    @Bean
    public Map<UInteger, MessageHandler> messageHandlerByAppId() {
        return new HashMap<>();
    }

    /**
     * Relationship registry<br/>
     * Key: listener port<br/>
     * Value: DatagramSocket
     */
    @Bean
    public Map<UShort, DatagramSocket> datagramSocketByListenerPort() {
        return new LinkedHashMap<>();
    }

    /**
     * Relationship registry<br/>
     * Key: port<br/>
     * Value: null or PortData
     */
    @Bean
    public Map<UShort, PortData> portDataByPort() {
        return new ConcurrentSkipListMap<>();
    }

    /**
     * Relationship registry<br/>
     * Key: port<br/>
     * Value: null or Partition
     */
    @Bean
    public Map<UShort, Partition> partitionByPort() {
        return new LinkedHashMap<>();
    }

    /**
     * Partition registry<br/>
     * Key: Partition ID<br/>
     * Value: Partition
     */
    @Bean
    public Map<Integer, Partition> partitionById() {
        return new LinkedHashMap<>();
    }

    /**
     * Relationship registry<br/>
     * Key: port<br/>
     * Value: null or Map&lt;Player name, Storage&gt;
     */
    @Bean
    public Map<UShort, Map<String, Storage>> playersViewByPort() {
        return new ConcurrentSkipListMap<>();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    /**
     * Pool size depends on count of @Scheduled methods and semantics in com.github.mbto.funnyranks.service.EventService
     */
    @Bean
    @DependsOn("distributorTE")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(1);

        scheduler.setThreadGroupName("schedulers");
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.setDaemon(false);
//        scheduler.setAllowCoreThreadTimeOut()
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(120);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return scheduler;
    }

    /**
     * consume from UDP port -> send to defaultPartition;
     */
    @Bean("receiverTE")
    @DependsOn("distributorTE")
    public ThreadPoolTaskExecutor receiverTE() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = 1;
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadGroupName("receivers");
        executor.setThreadNamePrefix("receiver-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

    /**
     * consume from defaultPartition -> distribute to partitions;
     */
    @Bean("distributorTE")
    public ThreadPoolTaskExecutor distributorTE() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = 1;
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadGroupName("distributors");
        executor.setThreadNamePrefix("distributor-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

    /**
     * Pool used in Receiver and MessagesConsumer classes
     * 1 - consume from partitions
     * -> accumulate players statistics and sessions
     * -> sending to senderTE pool;
     */
    @Bean
    @DependsOn("senderTE")
    public ThreadPoolTaskExecutor consumerTE() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        /* Pool sizes changes automatically, depends on the number of active partitions (in table funnyranks.port)
            AND the number of processors */
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(1);

        executor.setThreadGroupName("consumers");
        executor.setThreadNamePrefix("consumer-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

    /**
     * Pool used in SessionsSender class
     * 1 - consume players sessions from MessagesConsumer
     * -> merging players sessions into the funnyranks_stats.* tables.
     */
    @Bean
    @DependsOn("funnyRanksDsl")
    public ThreadPoolTaskExecutor senderTE(
            @Value("${funnyranks.datasource.maximumPoolSize}") int datasourceMaximumPoolSize
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int poolSize = Math.max(1,
                Math.min(datasourceMaximumPoolSize, Runtime.getRuntime().availableProcessors())
        );
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);

        executor.setThreadGroupName("sessionsSenders");
        executor.setThreadNamePrefix("sessionSender-");
        executor.setDaemon(false);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//        return builder -> {
//            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        };
//    }
    @Bean
    public ServletContextInitializer withParamsContextInitializer() {
        return servletContext -> {
//            servletContext.setInitParameter("javax.faces.DEFAULT_SUFFIX", ".xhtml");
            servletContext.setInitParameter("javax.faces.FACELETS_VIEW_MAPPINGS", "*.xhtml");
            servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
//        servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
            servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");
            servletContext.setInitParameter("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
            servletContext.setInitParameter("javax.faces.FACELETS_BUFFER_SIZE", "65535");

            // Bug in ELResolver https://stackoverflow.com/questions/19575283/jsf-2-2-interpret-empty-string-submitted-values-as-null-not-working
            // solutions don't work =/
            servletContext.setInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL", "true");

            servletContext.setInitParameter("com.sun.faces.enableRestoreView11Compatibility", "true");
            servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");

            servletContext.setInitParameter("primefaces.THEME", "glass-x");
            servletContext.setInitParameter("primefaces.SUBMIT", "partial");
            servletContext.setInitParameter("primefaces.TRANSFORM_METADATA", "true");
//            servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
            servletContext.setInitParameter("primefaces.UPLOADER", "native");

//            servletContext.setInitParameter("org.omnifaces.FACES_VIEWS_SCAN_PATHS", "/*.xhtml/*");
        };
    }

    @Bean
    public PasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}