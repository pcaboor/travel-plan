package com.travelplan.payment.support;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class JwtTestFactory {

    private final String secret;
    private final String issuer;

    public JwtTestFactory(String secret, String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    public String bearer(UUID userId, String email, List<String> roles) {
        try {
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(userId.toString())
                    .claim("email", email)
                    .claim("roles", roles)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                    .build();
            SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signed.sign(new MACSigner(key.getEncoded()));
            return "Bearer " + signed.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to mint test JWT", e);
        }
    }
}
