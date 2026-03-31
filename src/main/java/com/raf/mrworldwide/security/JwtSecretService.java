package com.raf.mrworldwide.security;

import com.raf.mrworldwide.exceptions.SecretNotFoundException;

/**
 * Abstraction for retrieving the JWT HS512 signing key.
 * Concrete implementations may source the key from AWS Secrets Manager,
 * a local properties file, or any other secret store.
 */
public interface JwtSecretService {

    /**
     * Returns the raw 512-bit (64-byte) key used to sign and verify JWT tokens.
     *
     * @return a 64-byte signing key; callers must not mutate the returned array
     * @throws SecretNotFoundException if the key cannot be retrieved
     */
    byte[] getJwtSigningSecret() throws SecretNotFoundException;
}

