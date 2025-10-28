package com.github.mbto.funnyranks;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildHikariDataSource;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.configurateJooqContext;

@Configuration
public class JooqConfig {
    @Bean
    @Lazy(false)
    DSLContext funnyRanksDsl(HikariDataSource funnyRanksDataSource,
                             @Value("${funnyranks.datasource.schema}") String schema
    ) {
        return configurateJooqContext(funnyRanksDataSource, FUNNYRANKS.getName(), schema);
    }

    @ConfigurationProperties("funnyranks.datasource")
    @Bean
    @DependsOn("distributorTE")
    public HikariDataSource funnyRanksDataSource(
            @Value("${funnyranks.datasource.schema}") String schema
    ) {
        return buildHikariDataSource("funnyranks-pool", schema);
    }
}