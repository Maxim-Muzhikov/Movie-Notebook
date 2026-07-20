package com.movienotebook.api.service;

import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	
	public User getByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с ником " + username + " не найден"));
	}
	
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}
	
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	
	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с номером " + id + " не найден"));
	}
	
	@Transactional
	public User save(User user) {
		return userRepository.save(user);
	}
	
	public User getReferenceById(Long id) {
		return userRepository.getReferenceById(id);
	}
}