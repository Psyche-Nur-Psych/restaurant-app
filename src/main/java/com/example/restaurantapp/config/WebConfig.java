package com.example.restaurantapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = new File("uploads").getAbsolutePath().replace("\\", "/");
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + dir, "file:" + dir);
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/", "file:///" + dir, "file:" + dir);
    }
}
