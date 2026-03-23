package com.example.newsService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.news-dir:uploads/news}")
    private String newsDir;

    @Value("${app.file.gallery-dir:uploads/news/gallary_images}")
    private String galleryDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absoluteNewsPath = Path.of(newsDir).toAbsolutePath().normalize();
        Path absoluteGalleryPath = Path.of(galleryDir).toAbsolutePath().normalize();
        Path absoluteUploadsRoot = absoluteNewsPath.getParent() != null
                ? absoluteNewsPath.getParent().toAbsolutePath().normalize()
                : absoluteNewsPath;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absoluteUploadsRoot.toString() + "/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/uploads/news/**")
                .addResourceLocations("file:" + absoluteNewsPath.toString() + "/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/uploads/news/gallary_images/**")
                .addResourceLocations("file:" + absoluteGalleryPath.toString() + "/")
                .setCachePeriod(3600);
    }
}
