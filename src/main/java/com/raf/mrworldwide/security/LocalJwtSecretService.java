package com.raf.mrworldwide.security;

import com.raf.mrworldwide.exceptions.SecretNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Test/integration stub implementation of {@link JwtSecretService}.
 * <p>
 * Active only under the {@code test}, {@code test_it}, and {@code integration}
 * Spring profiles. Returns a fixed 64-byte (512-bit) key that satisfies the
 * HS512 algorithm without connecting to any external secret store.
 * <p>
 * <strong>Never use this implementation in production.</strong>
 */
@Slf4j
@Service
@Profile("test | test_it | integration")
public class LocalJwtSecretService implements JwtSecretService {

    /**
     * Fixed 64-byte ASCII key (512 bits) used exclusively in test environments.
     * The key is intentionally human-readable so it is easy to audit; its
     * content has no security value outside of local/CI testing.
     */
    private static final byte[] TEST_KEY =
            "MRWorldwide-TestOnly-JwtSigningKey-DoNotUseInProd-64ByteKey!!!!!".getBytes(StandardCharsets.UTF_8);

    static {
        if (TEST_KEY.length != 64) {
            throw new IllegalStateException("LocalJwtSecretService TEST_KEY must be exactly 64 bytes but was " + TEST_KEY.length);
        }
    }

    @Override
    public byte[] getJwtSigningSecret() throws SecretNotFoundException {
        log.warn("LocalJwtSecretService is active — using a hardcoded test key. This must NOT run in production.");
        return TEST_KEY.clone();
    }
}

