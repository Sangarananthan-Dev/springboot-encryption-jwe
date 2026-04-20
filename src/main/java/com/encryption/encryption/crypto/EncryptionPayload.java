package com.encryption.encryption.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EncryptionPayload(
		@NotNull(message = "data is required")
		JsonNode data,
		@NotNull(message = "timestamp is required")
		Long timestamp,
		@NotBlank(message = "nonce is required")
		String nonce
) {
}
