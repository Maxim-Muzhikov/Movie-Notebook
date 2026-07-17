package com.movienotebook.api.service;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.LoginResponseDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.entity.Role;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.UserAlreadyExistsException;
import com.movienotebook.api.mapper.UserMapper;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	
	// Новые зависимости
	private final AuthenticationManager authenticationManager;
	private final CustomUserDetailsService userDetailsService;
	private final JwtService jwtService;
	
	@Transactional
	public UserResponseDto register(RegisterRequestDto request) {
		// ... (твой код регистрации остается без изменений) ...
		if (userService.existsByUsername(request.username())) {
			throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
		}
		if (userService.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
		}
		
		User user = new User();
		user.setUsername(request.username());
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(Role.ROLE_USER);
		if (request.agreementAccepted() == null || !request.agreementAccepted()) {
			throw new IllegalArgumentException("Невозможно зарегистрировать пользователя без согласия с политикой");
		}
		
		User savedUser = userService.save(user);
		return userMapper.toDto(savedUser);
	}
	
	public LoginResponseDto login(LoginRequestDto request) {
		
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password())
		);
		
		CustomUserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
		
		String jwtToken = jwtService.generateToken(userDetails);
		
		return new LoginResponseDto(jwtToken);
	}
}