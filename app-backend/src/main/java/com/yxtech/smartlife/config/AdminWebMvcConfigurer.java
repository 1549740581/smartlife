package com.yxtech.smartlife.config;

import com.yxtech.smartlife.auth.AdminAuthInterceptor;
import com.yxtech.smartlife.auth.CurrentAdminArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminWebMvcConfigurer implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final CurrentAdminArgumentResolver currentAdminArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**", "/api/admin-web/**")
                .excludePathPatterns("/api/admin/login", "/api/admin-web/qrcode", "/api/admin-web/qrcode/status", "/api/admin-web/qrcode/confirm");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentAdminArgumentResolver);
    }
}
