package com.travelplan.auth.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travelplan.bootstrap")
public record BootstrapProperties(
        String adminEmail,
        String adminPassword) {
}
