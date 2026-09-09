package com.appsdeveloperblog.todoapp.todo;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.repository.TodoRepository;
import com.appsdeveloperblog.todoapp.user.User;
import com.appsdeveloperblog.todoapp.user.UserRepository;

@Service
public class TodoService {

	private final TodoRepository todoRepository;
	private final UserRepository userRepository;

	public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
		this.todoRepository = todoRepository;
		this.userRepository = userRepository;
	}

	public List<Todo> getTodos(Principal principal) {
		User user = getCurrentUser(principal);
		return todoRepository.findAllByUserOrderByIdAsc(user);
	}

	public Todo getTodo(Long id, Principal principal) {
		User user = getCurrentUser(principal);
		return findTodoByIdAndUser(id, user);
	}

	public Todo createTodo(String title, LocalDate dueDate, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = new Todo(title, dueDate, user);
		return todoRepository.save(todo);
	}

	public Todo updateTodo(Long id, String title, boolean completed, LocalDate dueDate, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = findTodoByIdAndUser(id, user);
		todo.setTitle(title);
		todo.setCompleted(completed);
		todo.setDueDate(dueDate);
		return todoRepository.save(todo);
	}

	public void deleteTodo(Long id, Principal principal) {
		User user = getCurrentUser(principal);
		Todo todo = findTodoByIdAndUser(id, user);
		todoRepository.delete(todo);
	}

	private Todo findTodoByIdAndUser(Long id, User user) {
		return todoRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	private User getCurrentUser(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		return userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

}
