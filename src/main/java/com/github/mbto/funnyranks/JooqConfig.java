package com.github.mbto.funnyranks;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildHikariDataSource;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.configurateJooqContext;

@Configuration
public class JooqConfig {
    @Bean
    @Lazy(false)
    DSLContext funnyRanksDsl(HikariDataSource funnyRanksDataSource) {
        return configurateJooqContext(funnyRanksDataSource, FUNNYRANKS.getName(), null);
    }

    @ConfigurationProperties("funnyranks.datasource")
    @Bean
    @DependsOn("distributorTE")
    public HikariDataSource funnyRanksDataSource() {
        return buildHikariDataSource("funnyranks-pool", FUNNYRANKS.getName());
    }
}