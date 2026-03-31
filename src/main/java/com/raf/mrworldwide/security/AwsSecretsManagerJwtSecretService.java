package com.raf.mrworldwide.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.raf.mrworldwide.exceptions.SecretNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AWS Secrets Manager implementation of {@link JwtSecretService}.
 * <p>
 * Fetches the HS512 JWT signing key from AWS Secrets Manager on first use,
 * then caches it in-process for 24 hours to avoid repeated network calls.
 * The secret must already exist at the fixed path
 * {@code developers/mr-worldwide/global/jwt-signing-key} in the configured region.
 * <p>
 * Expected secret structure (JSON):
 * <pre>
 * { "jwt-signing-key": "<base64-encoded 64-byte key>" }
 * </pre>
 */
@Slf4j
@Service
@Profile("!test & !test_it & !integration")
public class AwsSecretsManagerJwtSecretService implements JwtSecretService {

    private static final String SECRET_NAME = "developers/mr-worldwide/global/jwt-signing-key";
    private static final String SECRET_KEY_FIELD = "jwt-signing-key";
    private static final String CACHE_KEY = "jwt-signing-key";

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;
    private final Cache<String, byte[]> keyCache;

    public AwsSecretsManagerJwtSecretService(
            SecretsManagerClient secretsManagerClient,
            ObjectMapper objectMapper
    ) {
        this.secretsManagerClient = secretsManagerClient;
        this.objectMapper = objectMapper;
        this.keyCache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1)
                .build();
    }

    /**
     * Retrieves the JWT signing secret.
     * Returns a cached copy if available, otherwise fetches it from AWS Secrets Manager.
     * Always returns a defensive clone to prevent cache mutation.
     *
     * @return 64-byte HS512 signing key
     * @throws SecretNotFoundException if the secret is absent or malformed in AWS
     */
    @Override
    public byte[] getJwtSigningSecret() throws SecretNotFoundException {
        try {
            byte[] key = keyCache.get(CACHE_KEY, k -> fetchKeyFromAws());
            if (key == null) {
                throw new SecretNotFoundException("JWT signing secret could not be loaded from AWS Secrets Manager: " + SECRET_NAME);
            }
            return key.clone();
        } catch (SecretNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SecretNotFoundException("Failed to retrieve JWT signing secret from cache/AWS", e);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private byte[] fetchKeyFromAws() {
        log.info("Fetching JWT signing key from AWS Secrets Manager: {}", SECRET_NAME);
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            byte[] key = parseAndValidate(response.secretString());
            log.info("JWT signing key successfully loaded from AWS Secrets Manager.");
            return key;
        } catch (ResourceNotFoundException e) {
            log.error("JWT signing secret not found in AWS Secrets Manager: {}", SECRET_NAME);
            throw new SecretNotFoundException(
                    "JWT signing secret does not exist in AWS Secrets Manager at path: " + SECRET_NAME, e);
        } catch (SecretNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching JWT signing key from AWS Secrets Manager: {}", SECRET_NAME, e);
            throw new SecretNotFoundException(
                    "Unexpected error while fetching JWT signing secret from AWS for path: " + SECRET_NAME, e);
        }
    }

    /**
     * Parses the raw JSON secret string, extracts the Base64-encoded key,
     * decodes it, and validates its length.
     */
    private byte[] parseAndValidate(String secretString) {
        if (secretString == null || secretString.isBlank()) {
            throw new SecretNotFoundException(
                    "AWS Secrets Manager returned an empty secret for: " + SECRET_NAME);
        }

        try {
            Map<String, Object> secretMap = objectMapper.readValue(secretString, new TypeReference<>() {});
            String signingKey = (String) secretMap.get(SECRET_KEY_FIELD);

            if (signingKey == null || signingKey.isBlank()) {
                throw new SecretNotFoundException(
                        "Secret at '" + SECRET_NAME + "' does not contain field '" + SECRET_KEY_FIELD + "' or it is empty.");
            }

            return signingKey.getBytes(StandardCharsets.UTF_8);
        } catch (SecretNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse JWT signing secret from '{}'. The secret may be corrupt.", SECRET_NAME, e);
            throw new SecretNotFoundException(
                    "Failed to parse JWT signing secret from AWS Secrets Manager at: " + SECRET_NAME, e);
        }
    }
}

