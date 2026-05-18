package com.travelplan.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travelplan.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        int accessTokenTtlMinutes) {
}
