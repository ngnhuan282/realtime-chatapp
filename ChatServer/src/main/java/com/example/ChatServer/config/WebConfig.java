package com.example.ChatServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép truy cập trực tiếp vào thư mục uploads qua trình duyệt/app
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
