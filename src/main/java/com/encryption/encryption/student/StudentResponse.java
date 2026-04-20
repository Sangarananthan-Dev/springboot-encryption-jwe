package com.encryption.encryption.student;

import java.time.OffsetDateTime;

public record StudentResponse(
		Long id,
		String name,
		String email,
		Integer age,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
