package com.github.mbto.funnyranks;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildHikariDataSource;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.configurateJooqContext;

@Profile("test")
@Configuration
public class JooqConfigTest {
    @Bean
    @Lazy(false)
    DSLContext funnyRanksAdminDsl(HikariDataSource funnyRanksAdminDataSource) {
        return configurateJooqContext(funnyRanksAdminDataSource, 10, FUNNYRANKS.getName(), null);
    }

    @ConfigurationProperties("funnyranks.admin.datasource")
    @Bean
    @DependsOn("distributorTE")
    public HikariDataSource funnyRanksAdminDataSource() {
        return buildHikariDataSource("funnyranks-admin-pool", FUNNYRANKS.getName());
    }
}