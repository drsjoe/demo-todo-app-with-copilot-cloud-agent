package com.appsdeveloperblog.todoapp.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void showRegistrationForm_returnsRegisterView() throws Exception {
		mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeExists("registrationRequest"));
	}

	@Test
	void registerUser_withValidData_createsAccountAndRedirects() throws Exception {
		when(userService.registerUser(any(RegistrationRequest.class)))
				.thenReturn(new User("John", "Doe", "john.doe@example.com", "encoded"));

		mockMvc.perform(post("/register")
				.param("firstName", "John")
				.param("lastName", "Doe")
				.param("email", "john.doe@example.com")
				.param("password", "Secret123")
				.param("confirmPassword", "Secret123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/register?success"));

		verify(userService).registerUser(any(RegistrationRequest.class));
	}

	@Test
	void registerUser_withMismatchedPasswords_returnsFormWithErrorAndDoesNotCreateAccount() throws Exception {
		mockMvc.perform(post("/register")
				.param("firstName", "John")
				.param("lastName", "Doe")
				.param("email", "john.doe@example.com")
				.param("password", "Secret123")
				.param("confirmPassword", "Different123"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasErrors("registrationRequest"));

		verify(userService, never()).registerUser(any(RegistrationRequest.class));
	}

	@Test
	void registerUser_withMissingRequiredFields_returnsFormWithErrorAndDoesNotCreateAccount() throws Exception {
		mockMvc.perform(post("/register")
				.param("firstName", "")
				.param("lastName", "")
				.param("email", "not-an-email")
				.param("password", "short")
				.param("confirmPassword", "short"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("registrationRequest", "firstName", "lastName", "email",
						"password"));

		verify(userService, never()).registerUser(any(RegistrationRequest.class));
	}

	@Test
	void registerUser_withExistingEmail_returnsFormWithErrorAndDoesNotCreateAccount() throws Exception {
		when(userService.registerUser(any(RegistrationRequest.class)))
				.thenThrow(new EmailAlreadyExistsException("john.doe@example.com"));

		mockMvc.perform(post("/register")
				.param("firstName", "John")
				.param("lastName", "Doe")
				.param("email", "john.doe@example.com")
				.param("password", "Secret123")
				.param("confirmPassword", "Secret123"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("registrationRequest", "email"));
	}

}
