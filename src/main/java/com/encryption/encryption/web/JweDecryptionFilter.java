package com.encryption.encryption.web;

import com.encryption.encryption.config.EncryptionProperties;
import com.encryption.encryption.crypto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JweDecryptionFilter extends OncePerRequestFilter {

	public static final String ENCRYPTION_HEADER = "X-Use-Encryption";
	private static final String ENCRYPTED_CONTENT_TYPE = "application/jose+json";

	private final EncryptionProperties encryptionProperties;
	private final ObjectMapper objectMapper;
	private final JweService jweService;
	private final EncryptionKeyService encryptionKeyService;
	private final ReplayProtectionService replayProtectionService;

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		if (!encryptionProperties.enabled()) {
			return true;
		}
		String header = request.getHeader(ENCRYPTION_HEADER);
		if (!"true".equalsIgnoreCase(header)) {
			return true;
		}
		if (HttpMethod.GET.matches(request.getMethod()) || HttpMethod.OPTIONS.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod())) {
			return true;
		}
		String contentType = request.getContentType();
		return !StringUtils.hasText(contentType) || !contentType.startsWith(ENCRYPTED_CONTENT_TYPE);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException {
		try {
			String requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
			EncryptedEnvelope envelope = objectMapper.readValue(requestBody, EncryptedEnvelope.class);
			EncryptionPayload encryptionPayload = jweService.decrypt(envelope.jwe(), encryptionKeyService.getServerPrivateKey(), EncryptionPayload.class);
			replayProtectionService.validate(encryptionPayload.timestamp(), encryptionPayload.nonce());
			byte[] decryptedData = objectMapper.writeValueAsBytes(encryptionPayload.data());
			CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, decryptedData);
			filterChain.doFilter(wrapped, response);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("{\"error\":\"INVALID_ENCRYPTED_REQUEST\",\"message\":\"" + sanitizeMessage(ex.getMessage()) + "\"}");
		}
	}

	private String sanitizeMessage(String message) {
		if (!StringUtils.hasText(message)) {
			return "Failed to decrypt request payload.";
		}
		return message.replace("\"", "'");
	}
}
