package com.encryption.encryption.web;

import com.encryption.encryption.config.EncryptionProperties;
import com.encryption.encryption.crypto.EncryptedEnvelope;
import com.encryption.encryption.crypto.EncryptionKeyService;
import com.encryption.encryption.crypto.JweService;
import java.security.interfaces.RSAPublicKey;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@RequiredArgsConstructor
public class JweEncryptionAdvice implements ResponseBodyAdvice<Object> {

	public static final String CLIENT_PUBLIC_KEY_HEADER = "X-Client-Public-Key";

	private final EncryptionProperties encryptionProperties;
	private final JweService jweService;
	private final EncryptionKeyService encryptionKeyService;

	@Override
	public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
		if (!encryptionProperties.enabled() || body == null || body instanceof EncryptedEnvelope) {
			return body;
		}
		String encryptionHeader = request.getHeaders().getFirst(JweDecryptionFilter.ENCRYPTION_HEADER);
		if (!"true".equalsIgnoreCase(encryptionHeader)) {
			return body;
		}
		String clientPublicKeyPem = request.getHeaders().getFirst(CLIENT_PUBLIC_KEY_HEADER);
		if (clientPublicKeyPem == null || clientPublicKeyPem.isBlank()) {
			return body;
		}
		RSAPublicKey clientPublicKey = encryptionKeyService.parseClientPublicKey(clientPublicKeyPem);
		String jwe = jweService.encrypt(body, clientPublicKey);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		return new EncryptedEnvelope(jwe);
	}
}
