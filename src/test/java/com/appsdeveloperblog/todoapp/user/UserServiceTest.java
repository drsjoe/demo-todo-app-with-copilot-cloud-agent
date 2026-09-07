package com.appsdeveloperblog.todoapp.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, passwordEncoder);
	}

	@Test
	void registerUser_savesUserWithEncodedPassword() {
		RegistrationRequest request = new RegistrationRequest("John", "Doe", "john.doe@example.com", "Secret123",
				"Secret123");
		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User savedUser = userService.registerUser(request);

		assertThat(savedUser.getFirstName()).isEqualTo("John");
		assertThat(savedUser.getLastName()).isEqualTo("Doe");
		assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
		assertThat(savedUser.getPassword()).isNotEqualTo("Secret123");
		assertThat(passwordEncoder.matches("Secret123", savedUser.getPassword())).isTrue();
		verify(userRepository).save(any(User.class));
	}

	@Test
	void registerUser_throwsWhenEmailAlreadyExists() {
		RegistrationRequest request = new RegistrationRequest("John", "Doe", "john.doe@example.com", "Secret123",
				"Secret123");
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(EmailAlreadyExistsException.class);
	}

}
