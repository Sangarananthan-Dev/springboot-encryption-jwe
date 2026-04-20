package com.encryption.encryption.crypto;

import com.encryption.encryption.config.EncryptionProperties;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionKeyService {

	private static final String BEGIN_PUBLIC = "-----BEGIN PUBLIC KEY-----";
	private static final String END_PUBLIC = "-----END PUBLIC KEY-----";
	private static final String BEGIN_PRIVATE = "-----BEGIN PRIVATE KEY-----";
	private static final String END_PRIVATE = "-----END PRIVATE KEY-----";

	private final EncryptionProperties encryptionProperties;

	@Getter
	private RSAPrivateKey serverPrivateKey;

	@Getter
	private RSAPublicKey serverPublicKey;

	@PostConstruct
	void init() {
		try {
			if (StringUtils.hasText(encryptionProperties.serverPrivateKeyPem()) && StringUtils.hasText(encryptionProperties.serverPublicKeyPem())) {
				serverPrivateKey = (RSAPrivateKey) parsePrivateKey(encryptionProperties.serverPrivateKeyPem());
				serverPublicKey = (RSAPublicKey) parsePublicKey(encryptionProperties.serverPublicKeyPem());
				log.info("Loaded configured RSA keys for JWE.");
				return;
			}
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair pair = generator.generateKeyPair();
			serverPrivateKey = (RSAPrivateKey) pair.getPrivate();
			serverPublicKey = (RSAPublicKey) pair.getPublic();
			log.warn("No static JWE keys configured. Generated ephemeral keys for this boot.");
			log.info("Server public key PEM (share with client):\n{}", toPublicPem(serverPublicKey));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to initialize encryption keys", ex);
		}
	}

	public String serverPublicKeyPem() {
		return toPublicPem(serverPublicKey);
	}

	public String serverPublicKeyBase64() {
		return Base64.getEncoder().encodeToString(serverPublicKey.getEncoded());
	}

	public RSAPublicKey parseClientPublicKey(String publicKeyPem) {
		try {
			String value = publicKeyPem.trim();
			if (value.startsWith(BEGIN_PUBLIC)) {
				return (RSAPublicKey) parsePublicKey(value);
			}
			byte[] keyBytes = Base64.getDecoder().decode(value);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid client public key", ex);
		}
	}

	private PublicKey parsePublicKey(String pem) throws Exception {
		byte[] keyBytes = parsePem(pem, BEGIN_PUBLIC, END_PUBLIC);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		return KeyFactory.getInstance("RSA").generatePublic(spec);
	}

	private PrivateKey parsePrivateKey(String pem) throws Exception {
		byte[] keyBytes = parsePem(pem, BEGIN_PRIVATE, END_PRIVATE);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		return KeyFactory.getInstance("RSA").generatePrivate(spec);
	}

	private byte[] parsePem(String pem, String begin, String end) {
		String normalized = pem.replace("\\n", "\n").replace("\r", "").replace("\n", "");
		String payload = normalized.replace(begin, "").replace(end, "").trim();
		return Base64.getDecoder().decode(payload.getBytes(StandardCharsets.UTF_8));
	}

	private String toPublicPem(RSAPublicKey publicKey) {
		String body = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
				.encodeToString(publicKey.getEncoded());
		return BEGIN_PUBLIC + "\n" + body + "\n" + END_PUBLIC;
	}
}
