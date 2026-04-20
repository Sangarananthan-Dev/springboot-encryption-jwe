package com.encryption.encryption.student;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;

	@Transactional
	public StudentResponse create(StudentRequest request) {
		studentRepository.findByEmail(request.email()).ifPresent(existing -> {
			throw new IllegalArgumentException("Student already exists with email: " + request.email());
		});
		OffsetDateTime now = OffsetDateTime.now();
		Student student = new Student();
		student.setName(request.name().trim());
		student.setEmail(request.email().trim().toLowerCase());
		student.setAge(request.age());
		student.setCreatedAt(now);
		student.setUpdatedAt(now);
		return toResponse(studentRepository.save(student));
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> list() {
		return studentRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public StudentResponse getById(Long id) {
		Student student = studentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
		return toResponse(student);
	}

	@Transactional
	public StudentResponse update(Long id, StudentRequest request) {
		Student student = studentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
		studentRepository.findByEmail(request.email()).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new IllegalArgumentException("Student already exists with email: " + request.email());
			}
		});
		student.setName(request.name().trim());
		student.setEmail(request.email().trim().toLowerCase());
		student.setAge(request.age());
		student.setUpdatedAt(OffsetDateTime.now());
		return toResponse(studentRepository.save(student));
	}

	@Transactional
	public void delete(Long id) {
		Student student = studentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
		studentRepository.delete(student);
	}

	private StudentResponse toResponse(Student student) {
		return new StudentResponse(
				student.getId(),
				student.getName(),
				student.getEmail(),
				student.getAge(),
				student.getCreatedAt(),
				student.getUpdatedAt()
		);
	}
}
