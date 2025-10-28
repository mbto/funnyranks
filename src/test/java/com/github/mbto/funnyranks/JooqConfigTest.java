package com.github.mbto.funnyranks;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
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
    DSLContext funnyRanksAdminDsl(HikariDataSource funnyRanksAdminDataSource,
                                  @Value("${funnyranks.admin.datasource.schema}") String schema
    ) {
        return configurateJooqContext(funnyRanksAdminDataSource, 10, FUNNYRANKS.getName(), schema);
    }

    @ConfigurationProperties("funnyranks.admin.datasource")
    @Bean
    @DependsOn("distributorTE")
    public HikariDataSource funnyRanksAdminDataSource(
            @Value("${funnyranks.admin.datasource.schema}") String schema
    ) {
        return buildHikariDataSource("funnyranks-admin-pool", schema);
    }
}