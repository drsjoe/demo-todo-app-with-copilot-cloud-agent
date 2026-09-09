package com.appsdeveloperblog.todoapp.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User registerUser(RegistrationRequest registrationRequest) {
		if (userRepository.existsByEmail(registrationRequest.email())) {
			throw new EmailAlreadyExistsException(registrationRequest.email());
		}

		String encodedPassword = passwordEncoder.encode(registrationRequest.password());
		User user = new User(registrationRequest.firstName(), registrationRequest.lastName(),
				registrationRequest.email(), encodedPassword);

		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException exception) {
			if (userRepository.existsByEmail(registrationRequest.email())) {
				throw new EmailAlreadyExistsException(registrationRequest.email());
			}
			throw exception;
		}
	}

}
