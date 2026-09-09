package com.appsdeveloperblog.todoapp.todo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.user.User;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TodoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TodoService todoService;

	@Test
	void getTodos_returnsTodosForAuthenticatedUser() throws Exception {
		Todo todo = new Todo("Pay bills", LocalDate.of(2026, 9, 10), new User("John", "Doe", "john.doe@example.com", "encoded"));
		when(todoService.getTodos(any())).thenReturn(List.of(todo));

		mockMvc.perform(get("/api/todos").principal(() -> "john.doe@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Pay bills"))
				.andExpect(jsonPath("$[0].completed").value(false))
				.andExpect(jsonPath("$[0].dueDate").value("2026-09-10"));
	}

	@Test
	void getTodo_returnsNotFoundWhenServiceThrowsNotFound() throws Exception {
		when(todoService.getTodo(eq(42L), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/api/todos/{id}", 42L).principal(() -> "john.doe@example.com"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createTodo_returnsCreatedTodo() throws Exception {
		Todo todo = new Todo("Pay bills", LocalDate.of(2026, 9, 10), new User("John", "Doe", "john.doe@example.com", "encoded"));
		when(todoService.createTodo(eq("Pay bills"), eq(LocalDate.of(2026, 9, 10)), any())).thenReturn(todo);

		mockMvc.perform(post("/api/todos")
				.principal(() -> "john.doe@example.com")
				.contentType("application/json")
				.content("""
						{"title":"Pay bills","dueDate":"2026-09-10"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Pay bills"))
				.andExpect(jsonPath("$.completed").value(false))
				.andExpect(jsonPath("$.dueDate").value("2026-09-10"));
	}

	@Test
	void updateTodo_returnsUpdatedTodo() throws Exception {
		Todo todo = new Todo("Updated", LocalDate.of(2026, 9, 30), new User("John", "Doe", "john.doe@example.com", "encoded"));
		todo.setCompleted(true);
		when(todoService.updateTodo(eq(7L), eq("Updated"), eq(true), eq(LocalDate.of(2026, 9, 30)), any()))
				.thenReturn(todo);

		mockMvc.perform(put("/api/todos/{id}", 7L)
				.principal(() -> "john.doe@example.com")
				.contentType("application/json")
				.content("""
						{"title":"Updated","completed":true,"dueDate":"2026-09-30"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated"))
				.andExpect(jsonPath("$.completed").value(true))
				.andExpect(jsonPath("$.dueDate").value("2026-09-30"));
	}

	@Test
	void deleteTodo_returnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/todos/{id}", 9L).principal(() -> "john.doe@example.com"))
				.andExpect(status().isNoContent());

		verify(todoService).deleteTodo(eq(9L), any());
	}

}
