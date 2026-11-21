package com.ua.bikeshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Веб-конфігурація.
 *
 * CORS відкриває /api/** для React dev-сервера (Vite, http://localhost:5173),
 * який працює на іншому порту. UI додається окремою самостійною роботою, але
 * налаштування вже готове.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
