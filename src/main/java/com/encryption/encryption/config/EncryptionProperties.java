package com.encryption.encryption.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.encryption")
public record EncryptionProperties(
		boolean enabled,
		String clientOrigin,
		long timestampSkewSeconds,
		long nonceTtlSeconds,
		String serverPrivateKeyPem,
		String serverPublicKeyPem
) {
}
