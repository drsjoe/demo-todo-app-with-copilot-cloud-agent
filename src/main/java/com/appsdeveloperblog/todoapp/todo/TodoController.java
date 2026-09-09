package com.appsdeveloperblog.todoapp.todo;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

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

import com.appsdeveloperblog.todoapp.entity.Todo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

	private final TodoService todoService;

	public TodoController(TodoService todoService) {
		this.todoService = todoService;
	}

	@GetMapping
	public List<TodoResponse> getTodos(Principal principal) {
		return todoService.getTodos(principal).stream()
				.map(this::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public TodoResponse getTodo(@PathVariable Long id, Principal principal) {
		return toResponse(todoService.getTodo(id, principal));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TodoResponse createTodo(@Valid @RequestBody TodoCreateRequest request, Principal principal) {
		Todo todo = todoService.createTodo(request.title(), request.dueDate(), principal);
		return toResponse(todo);
	}

	@PutMapping("/{id}")
	public TodoResponse updateTodo(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request,
			Principal principal) {
		Todo todo = todoService.updateTodo(id, request.title(), request.completed(), request.dueDate(), principal);
		return toResponse(todo);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTodo(@PathVariable Long id, Principal principal) {
		todoService.deleteTodo(id, principal);
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
