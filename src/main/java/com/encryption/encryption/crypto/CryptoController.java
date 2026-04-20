package com.encryption.encryption.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoController {

	private final EncryptionKeyService encryptionKeyService;

	@GetMapping("/public-key")
	public PublicKeyResponse serverPublicKey() {
		return new PublicKeyResponse(
				encryptionKeyService.serverPublicKeyPem(),
				encryptionKeyService.serverPublicKeyBase64()
		);
	}

	public record PublicKeyResponse(String pem, String base64Spki) {
	}
}
