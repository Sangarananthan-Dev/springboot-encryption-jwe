package com.encryption.encryption.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(
		@NotBlank(message = "name is required")
		@Size(max = 120, message = "name cannot exceed 120 characters")
		String name,
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		@Size(max = 200, message = "email cannot exceed 200 characters")
		String email,
		@NotNull(message = "age is required")
		@Min(value = 3, message = "age should be >= 3")
		@Max(value = 120, message = "age should be <= 120")
		Integer age
) {
}
