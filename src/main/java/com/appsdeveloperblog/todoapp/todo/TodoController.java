package com.appsdeveloperblog.todoapp.todo;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.repository.TodoRepository;
import com.appsdeveloperblog.todoapp.user.User;
import com.appsdeveloperblog.todoapp.user.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

	private final TodoRepository todoRepository;
	private final UserRepository userRepository;

	public TodoController(TodoRepository todoRepository, UserRepository userRepository) {
		this.todoRepository = todoRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<TodoResponse> getTodos(Principal principal) {
		User user = getCurrentUser(principal);
		return todoRepository.findAllByUserOrderByIdAsc(user).stream()
				.map(this::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public TodoResponse getTodo(@PathVariable Long id, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = todoRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return toResponse(todo);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TodoResponse createTodo(@Valid @RequestBody TodoCreateRequest request, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = new Todo(request.title(), request.dueDate(), user);
		return toResponse(todoRepository.save(todo));
	}

	@PutMapping("/{id}")
	public TodoResponse updateTodo(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = todoRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		todo.setTitle(request.title());
		todo.setCompleted(request.completed());
		todo.setDueDate(request.dueDate());
		return toResponse(todoRepository.save(todo));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTodo(@PathVariable Long id, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = todoRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		todoRepository.delete(todo);
	}

	private User getCurrentUser(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		return userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	private TodoResponse toResponse(Todo todo) {
		return new TodoResponse(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getDueDate());
	}

	record TodoCreateRequest(@NotBlank String title, LocalDate dueDate) {
	}

	record TodoUpdateRequest(@NotBlank String title, boolean completed, LocalDate dueDate) {
	}

	record TodoResponse(Long id, String title, boolean completed, LocalDate dueDate) {
	}

}
