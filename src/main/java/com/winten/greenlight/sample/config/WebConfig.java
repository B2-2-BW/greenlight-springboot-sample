package com.winten.greenlight.sample.config;

import com.winten.greenlight.sample.interceptor.SampleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final SampleInterceptor sampleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sampleInterceptor).addPathPatterns("/**")
                .addPathPatterns("/**");
    }
}