package com.example.ChatServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép truy cập trực tiếp vào thư mục uploads qua trình duyệt/app
        Path uploadsPath = resolveChatServerUploadsPath();
        Path legacyUploadsPath = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        uploadsPath.toUri().toString(),
                        legacyUploadsPath.toUri().toString()
                );
    }

    private Path resolveChatServerUploadsPath() {
        Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path cursor = currentDir;
        while (cursor != null) {
            Path name = cursor.getFileName();
            if (name != null && "ChatServer".equalsIgnoreCase(name.toString())) {
                return cursor.resolve("uploads");
            }

            Path chatServerDir = cursor.resolve("ChatServer");
            if (Files.isDirectory(chatServerDir)) {
                return chatServerDir.resolve("uploads");
            }

            cursor = cursor.getParent();
        }

        return currentDir.resolve("uploads");
    }
}
