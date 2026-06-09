package com.enstud.common.config;

import com.enstud.common.AuthRequiredInterceptor;
import com.enstud.common.SecurityContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：CORS 跨域 + 拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 清理 SecurityContext（必须最后执行）
        registry.addInterceptor(new SecurityContextInterceptor())
                .addPathPatterns("/**");

        // 用户认证拦截器（在请求到达 Controller 前检查登录）
        registry.addInterceptor(new AuthRequiredInterceptor())
                .addPathPatterns("/**");
    }
}
