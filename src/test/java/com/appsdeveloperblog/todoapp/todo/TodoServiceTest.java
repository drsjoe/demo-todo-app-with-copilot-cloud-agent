package com.appsdeveloperblog.todoapp.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.repository.TodoRepository;
import com.appsdeveloperblog.todoapp.user.User;
import com.appsdeveloperblog.todoapp.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private UserRepository userRepository;

	private TodoService todoService;

	@BeforeEach
	void setUp() {
		todoService = new TodoService(todoRepository, userRepository);
	}

	@Test
	void getTodos_returnsTodosForCurrentUser() {
		User user = new User("John", "Doe", "john.doe@example.com", "encoded");
		Principal principal = () -> "john.doe@example.com";
		Todo todo = new Todo("Pay bills", LocalDate.of(2026, 9, 10), user);
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
		when(todoRepository.findAllByUserOrderByIdAsc(user)).thenReturn(List.of(todo));

		List<Todo> todos = todoService.getTodos(principal);

		assertThat(todos).containsExactly(todo);
	}

	@Test
	void createTodo_savesTodoForCurrentUser() {
		User user = new User("John", "Doe", "john.doe@example.com", "encoded");
		Principal principal = () -> "john.doe@example.com";
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
		when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Todo created = todoService.createTodo("Pay bills", LocalDate.of(2026, 9, 10), principal);

		assertThat(created.getTitle()).isEqualTo("Pay bills");
		assertThat(created.getDueDate()).isEqualTo(LocalDate.of(2026, 9, 10));
		assertThat(created.isCompleted()).isFalse();
		assertThat(created.getUser()).isEqualTo(user);
		verify(todoRepository).save(any(Todo.class));
	}

	@Test
	void updateTodo_updatesExistingTodoForCurrentUser() {
		User user = new User("John", "Doe", "john.doe@example.com", "encoded");
		Principal principal = () -> "john.doe@example.com";
		Todo existing = new Todo("Old title", null, user);
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
		when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
		when(todoRepository.save(existing)).thenReturn(existing);

		Todo updated = todoService.updateTodo(1L, "New title", true, LocalDate.of(2026, 9, 30), principal);

		assertThat(updated.getTitle()).isEqualTo("New title");
		assertThat(updated.isCompleted()).isTrue();
		assertThat(updated.getDueDate()).isEqualTo(LocalDate.of(2026, 9, 30));
		verify(todoRepository).save(existing);
	}

	@Test
	void deleteTodo_deletesExistingTodoForCurrentUser() {
		User user = new User("John", "Doe", "john.doe@example.com", "encoded");
		Principal principal = () -> "john.doe@example.com";
		Todo existing = new Todo("Pay bills", null, user);
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
		when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));

		todoService.deleteTodo(1L, principal);

		verify(todoRepository).delete(existing);
	}

	@Test
	void getTodo_throwsNotFoundWhenTodoDoesNotExistForCurrentUser() {
		User user = new User("John", "Doe", "john.doe@example.com", "encoded");
		Principal principal = () -> "john.doe@example.com";
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
		when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> todoService.getTodo(1L, principal));

		assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getTodos_throwsUnauthorizedWhenPrincipalIsMissing() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> todoService.getTodos(null));

		assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void getTodos_throwsUnauthorizedWhenUserIsMissing() {
		Principal principal = () -> "john.doe@example.com";
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> todoService.getTodos(principal));

		assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
