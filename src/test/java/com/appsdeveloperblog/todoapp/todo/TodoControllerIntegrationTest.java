package com.appsdeveloperblog.todoapp.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.repository.TodoRepository;
import com.appsdeveloperblog.todoapp.user.User;
import com.appsdeveloperblog.todoapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TodoRepository todoRepository;

	private User john;
	private User jane;

	@BeforeEach
	void setUp() {
		todoRepository.deleteAll();
		userRepository.deleteAll();
		john = userRepository.save(new User("John", "Doe", "john.doe@example.com", "password"));
		jane = userRepository.save(new User("Jane", "Doe", "jane.doe@example.com", "password"));
	}

	@Test
	void getTodos_requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/todos"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createTodo_createsTodoForAuthenticatedUserWithDefaultCompleted() throws Exception {
		mockMvc.perform(post("/api/todos")
				.with(user("john.doe@example.com"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateTodoRequest("Pay bills", LocalDate.of(2026, 9, 10)))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Pay bills"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.dueDate").value("2026-09-10"));

		Todo savedTodo = todoRepository.findAll().getFirst();
		assertThat(savedTodo.isCompleted()).isFalse();
		assertThat(savedTodo.getUser().getId()).isEqualTo(john.getId());
	}

	@Test
	void crudOperations_areScopedToCurrentUser() throws Exception {
		Todo johnTodo = todoRepository.save(new Todo("John todo", LocalDate.of(2026, 9, 11), john));
		todoRepository.save(new Todo("Jane todo", null, jane));

		mockMvc.perform(get("/api/todos").with(user("john.doe@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("John todo"))
				.andExpect(jsonPath("$[1]").doesNotExist());

		mockMvc.perform(get("/api/todos/{id}", johnTodo.getId()).with(user("jane.doe@example.com")))
				.andExpect(status().isNotFound());

		mockMvc.perform(put("/api/todos/{id}", johnTodo.getId())
				.with(user("john.doe@example.com"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UpdateTodoRequest("Updated", true, null))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated"))
				.andExpect(jsonPath("$.completed").value(true));

		mockMvc.perform(delete("/api/todos/{id}", johnTodo.getId())
				.with(user("john.doe@example.com"))
				.with(csrf()))
				.andExpect(status().isNoContent());

		assertThat(todoRepository.findById(johnTodo.getId())).isEmpty();
	}

	record CreateTodoRequest(String title, LocalDate dueDate) {
	}

	record UpdateTodoRequest(String title, boolean completed, LocalDate dueDate) {
	}

}
