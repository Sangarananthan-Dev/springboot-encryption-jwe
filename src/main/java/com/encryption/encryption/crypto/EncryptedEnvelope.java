package com.encryption.encryption.crypto;

import jakarta.validation.constraints.NotBlank;

public record EncryptedEnvelope(
		@NotBlank(message = "jwe is required")
		String jwe
) {
}
