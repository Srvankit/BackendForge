package com.thecreatorco.backendforge.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfiguration implements WebMvcConfigurer {

    private final ClientRateLimiter clientRateLimiter;

    public RateLimitConfiguration(ClientRateLimiter clientRateLimiter) {
        this.clientRateLimiter = clientRateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(clientRateLimiter));
    }
}
