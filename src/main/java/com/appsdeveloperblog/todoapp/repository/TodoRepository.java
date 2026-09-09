package com.appsdeveloperblog.todoapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appsdeveloperblog.todoapp.entity.Todo;
import com.appsdeveloperblog.todoapp.user.User;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	List<Todo> findAllByUserOrderByIdAsc(User user);

	Optional<Todo> findByIdAndUser(Long id, User user);

}
