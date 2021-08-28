package com.github.mbto.funnyranks.webapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DefaultView implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/brokers").setViewName("forward:/brokers.xhtml");
        registry.addViewController("/editBroker").setViewName("forward:/editBroker.xhtml");
        registry.addViewController("/editProfile").setViewName("forward:/editProfile.xhtml");
        registry.addViewController("/editProject").setViewName("forward:/editProject.xhtml");
        registry.addViewController("/games").setViewName("forward:/games.xhtml");
        registry.addViewController("/identities").setViewName("forward:/identities.xhtml");
        registry.addViewController("/").setViewName("forward:/index.xhtml");
        registry.addViewController("/login").setViewName("forward:/login.xhtml");
        registry.addViewController("/managers").setViewName("forward:/managers.xhtml");
        registry.addViewController("/newBroker").setViewName("forward:/newBroker.xhtml");
        registry.addViewController("/newProject").setViewName("forward:/newProject.xhtml");
        registry.addViewController("/portsByProject").setViewName("forward:/portsByProject.xhtml");
        registry.addViewController("/projects").setViewName("forward:/projects.xhtml");
    }
}