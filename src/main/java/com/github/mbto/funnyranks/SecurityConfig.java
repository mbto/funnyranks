package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.service.ManagerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ManagerService managerService;

    public SecurityConfig(ManagerService managerService) {
        this.managerService = managerService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form
                        .loginPage("/login.xhtml")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login.xhtml?failed")
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/favicon.ico",
                                        "/css/**",
                                        "/images/**",
                                        "/jakarta.faces.resource/**",
                                        "/logout",
                                        "/403.html",
                                        "/404.html",
                                        "/500.html"
                                )
                                    .permitAll()
                                .requestMatchers(
                                        "/brokers**",
                                        "/editBroker**",
                                        "/games**",
                                        "/managers**",
                                        "/new**"
                                )
                                    .hasRole("broker")
                                .requestMatchers(
                                        "/editProfile**",
                                        "/editProject**",
                                        "/identities**",
                                        "/",
                                        "/index**",
                                        "/portsByProject**",
                                        "/projects**"
                                )
                                    .hasAnyRole(
                                            "broker",
                                            "project"
                                    )
                                .requestMatchers("/login**")
                                    .anonymous()
                                .anyRequest()
//                            .denyAll()
                                .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                )
                .rememberMe(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .userDetailsService(managerService)
                .csrf(AbstractHttpConfigurer::disable)
//                .jee(AbstractHttpConfigurer::disable)
//                .x509(AbstractHttpConfigurer::disable)
//                .cors(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.accessDeniedPage("/403.html"));
        return http.build();
    }

    @Bean
    public PasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}