package com.hcmcyu.member.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String avatarPath;
    private final String bankQrPath;
    private final String avatarPublicUrlPrefix;
    private final String bankQrPublicUrlPrefix;

    public WebMvcConfig(
            @Value("${storage.local.avatar-path:storage/avatars}") String avatarPath,
            @Value("${storage.local.bank-qr-path:storage/bank-qr}") String bankQrPath,
            @Value("${storage.public-url-prefix:/uploads/avatars}") String avatarPublicUrlPrefix,
            @Value("${storage.bank-qr-public-url-prefix:/uploads/bank-qr}") String bankQrPublicUrlPrefix
    ) {
        this.avatarPath = avatarPath;
        this.bankQrPath = bankQrPath;
        this.avatarPublicUrlPrefix = stripTrailingSlash(avatarPublicUrlPrefix);
        this.bankQrPublicUrlPrefix = stripTrailingSlash(bankQrPublicUrlPrefix);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(avatarPublicUrlPrefix + "/**")
                .addResourceLocations(resourceLocation(avatarPath));
        registry.addResourceHandler(bankQrPublicUrlPrefix + "/**")
                .addResourceLocations(resourceLocation(bankQrPath));
    }

    private String resourceLocation(String storagePath) {
        String location = Path.of(storagePath).toAbsolutePath().normalize().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    private String stripTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
