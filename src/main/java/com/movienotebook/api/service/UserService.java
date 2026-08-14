package com.movienotebook.api.service;

import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.UserMapper;
import com.movienotebook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	
	public UserResponseDto getByUsername(String username) {
		return userMapper.toDto(userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с ником " + username + " не найден")));
	}
	
	public UserResponseDto getById(Long id) {
		return userMapper.toDto(userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с номером " + id + " не найден")));
	}
	
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}
	
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	
	User getEntityByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с ником " + username + " не найден"));
	}
	
	User getEntityById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с номером " + id + " не найден"));
	}
	
	User getReferenceById(Long id) {
		return userRepository.getReferenceById(id);
	}
	
	@Transactional
	User save(User user) {
		return userRepository.save(user);
	}
}