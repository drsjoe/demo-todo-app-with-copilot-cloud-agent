package com.appsdeveloperblog.todoapp.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LoginFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void showLoginPage_returnsLoginView() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}

	@Test
	void tbdPage_requiresAuthentication() throws Exception {
		mockMvc.perform(get("/tbd"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void registerUser_signsUserInAndRedirectsToTbd() throws Exception {
		MvcResult result = mockMvc.perform(post("/register")
				.with(csrf())
				.param("firstName", "John")
				.param("lastName", "Doe")
				.param("email", "john.doe@example.com")
				.param("password", "Secret123")
				.param("confirmPassword", "Secret123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/tbd"))
				.andExpect(authenticated().withUsername("john.doe@example.com"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/tbd").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("tbd"))
				.andExpect(model().attribute("email", "john.doe@example.com"));

		User savedUser = userRepository.findByEmail("john.doe@example.com").orElseThrow();
		assertThat(savedUser.getPassword()).isNotEqualTo("Secret123");
		assertThat(passwordEncoder.matches("Secret123", savedUser.getPassword())).isTrue();
	}

	@Test
	void loginWithValidCredentials_redirectsToTbd() throws Exception {
		userRepository.save(new User("John", "Doe", "john.doe@example.com", passwordEncoder.encode("Secret123")));

		MvcResult result = mockMvc.perform(formLogin("/login").user("email", "john.doe@example.com")
				.password("Secret123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/tbd"))
				.andExpect(authenticated().withUsername("john.doe@example.com"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/tbd").session(session))
				.andExpect(status().isOk())
				.andExpect(model().attribute("email", "john.doe@example.com"));
	}

	@Test
	void loginWithInvalidCredentials_redirectsToLoginError() throws Exception {
		userRepository.save(new User("John", "Doe", "john.doe@example.com", passwordEncoder.encode("Secret123")));

		mockMvc.perform(formLogin("/login").user("email", "john.doe@example.com")
				.password("WrongPassword"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

}
