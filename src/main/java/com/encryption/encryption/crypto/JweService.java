package com.encryption.encryption.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JweService {

	private final ObjectMapper objectMapper;

	public String encrypt(Object payload, RSAPublicKey publicKey) {
		try {
			String json = objectMapper.writeValueAsString(payload);
			JWEObject jweObject = new JWEObject(
					new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).build(),
					new Payload(json)
			);
			jweObject.encrypt(new RSAEncrypter(publicKey));
			return jweObject.serialize();
		} catch (JOSEException | JsonProcessingException ex) {
			throw new IllegalStateException("Failed to encrypt payload", ex);
		}
	}

	public <T> T decrypt(String serializedJwe, RSAPrivateKey privateKey, Class<T> targetType) {
		try {
			JWEObject jweObject = JWEObject.parse(serializedJwe);
			jweObject.decrypt(new RSADecrypter(privateKey));
			String json = jweObject.getPayload().toString();
			return objectMapper.readValue(json, targetType);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to decrypt payload", ex);
		}
	}
}
