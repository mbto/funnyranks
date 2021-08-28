package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ManagerService managerService;
    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .formLogin()
                .loginPage("/login.xhtml")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login.xhtml?failed")
        .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.xhtml")
                .invalidateHttpSession(true)
        .and()
            .rememberMe()
                .tokenRepository(new InMemoryTokenRepositoryImpl())
                .alwaysRemember(true)
        .and()
//            .x509().disable()
//            .cors().disable()
            .csrf().disable()
//            .jee().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        .and().authorizeRequests()
            .antMatchers("/css/**","/images/**","/javax.faces.resource/**","/403.html").permitAll()
            .antMatchers("/brokers**","/editBroker**","/games**","/managers**","/new**").hasRole("broker")
            .antMatchers("/editProfile**","/editProject**","/identities**","/","/index**","/portsByProject**","/projects**").hasAnyRole("broker","project")
            .antMatchers("/login**").anonymous()
            .anyRequest().denyAll()
        .and()
            .exceptionHandling()
            .accessDeniedPage("/403.html")
        ;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(managerService)
                .passwordEncoder(bcryptPasswordEncoder);
    }
}