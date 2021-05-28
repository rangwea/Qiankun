package com.wikia.calabash.redirect;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private RedirectInterceptor redirectInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	// 添加拦截器，配置拦截地址
        registry.addInterceptor(redirectInterceptor).addPathPatterns("/**");
    }
}
