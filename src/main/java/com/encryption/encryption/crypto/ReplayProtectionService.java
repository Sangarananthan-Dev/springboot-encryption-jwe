package com.encryption.encryption.crypto;

import com.encryption.encryption.config.EncryptionProperties;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplayProtectionService {

	private final EncryptionProperties encryptionProperties;
	private final Map<String, Long> nonceStore = new ConcurrentHashMap<>();

	public void validate(long timestampMillis, String nonce) {
		long now = Instant.now().toEpochMilli();
		long skewMillis = encryptionProperties.timestampSkewSeconds() * 1000L;
		if (Math.abs(now - timestampMillis) > skewMillis) {
			throw new IllegalArgumentException("Encrypted payload timestamp is outside allowed skew.");
		}
		evictExpired(now);
		Long previous = nonceStore.putIfAbsent(nonce, now + (encryptionProperties.nonceTtlSeconds() * 1000L));
		if (previous != null) {
			throw new IllegalArgumentException("Replay detected: nonce already used.");
		}
	}

	private void evictExpired(long now) {
        nonceStore.entrySet().removeIf(next -> next.getValue() <= now);
	}
}
