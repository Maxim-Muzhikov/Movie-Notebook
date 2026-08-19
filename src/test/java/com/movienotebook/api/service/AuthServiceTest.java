package com.movienotebook.api.service;

import com.movienotebook.api.dto.auth.LoginRequestDto;
import com.movienotebook.api.dto.auth.LoginResponseDto;
import com.movienotebook.api.dto.auth.RegisterRequestDto;
import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.UserAlreadyExistsException;
import com.movienotebook.api.mapper.UserMapper;
import com.movienotebook.api.security.CustomUserDetails;
import com.movienotebook.api.security.CustomUserDetailsService;
import com.movienotebook.api.security.JwtService;
import com.movienotebook.api.util.ClassesExamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	
	@Mock
	private UserService userService;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Mock
	private UserMapper userMapper;
	
	@Mock
	private AuthenticationManager authenticationManager;
	
	@Mock
	private CustomUserDetailsService userDetailsService;
	
	@Mock
	private JwtService jwtService;
	
	@InjectMocks
	private AuthService authService;
	
	@Captor
	private ArgumentCaptor<User> userCaptor;
	
	private User existingUser;
	
	@BeforeEach
	void setUp() {
		existingUser = ClassesExamples.getExistingUser();
	}
	
	@Nested
	@DisplayName("Тесты метода register")
	class RegisterTests {
		
		@Test
		@DisplayName("Если имя пользователя уже занято, должен выбросить исключение UserAlreadyExistsException")
		void register_whenUsernameAlreadyExists_shouldThrowUserAlreadyExistsException() {
			// Arrange
			RegisterRequestDto request = new RegisterRequestDto(
					existingUser.getUsername(),
					"new_email@mail.ru",
					"password123",
					true
			);
			
			when(userService.existsByUsername(request.username())).thenReturn(true);
			
			// Act & Assert
			assertThatThrownBy(() -> authService.register(request))
					.isInstanceOf(UserAlreadyExistsException.class);
		}
		
		@Test
		@DisplayName("Если email уже занят, должен выбросить исключение UserAlreadyExistsException")
		void register_whenEmailAlreadyExists_shouldThrowUserAlreadyExistsException() {
			// Arrange
			RegisterRequestDto request = new RegisterRequestDto(
					"newUsername",
					existingUser.getEmail(),
					"password123",
					true
			);
			
			when(userService.existsByUsername(request.username())).thenReturn(false);
			when(userService.existsByEmail(request.email())).thenReturn(true);
			
			// Act & Assert
			assertThatThrownBy(() -> authService.register(request))
					.isInstanceOf(UserAlreadyExistsException.class);
		}
		
		@Test
		@DisplayName("Если пользователь не дал согласие с политикой, должен выбросить исключение IllegalArgumentException")
		void register_whenAgreementNotAccepted_shouldThrowIllegalArgumentException() {
			// Arrange
			RegisterRequestDto request = new RegisterRequestDto(
					"newUsername",
					"new_email@mail.ru",
					"password123",
					false
			);
			
			when(userService.existsByUsername(request.username())).thenReturn(false);
			when(userService.existsByEmail(request.email())).thenReturn(false);
			
			// Act & Assert
			assertThatThrownBy(() -> authService.register(request))
					.isInstanceOf(IllegalArgumentException.class);
		}
		
		@Test
		@DisplayName("Если согласие с политикой равно null, должен выбросить исключение IllegalArgumentException")
		void register_whenAgreementIsNull_shouldThrowIllegalArgumentException() {
			// Arrange
			RegisterRequestDto request = new RegisterRequestDto(
					"newUsername",
					"new_email@mail.ru",
					"password123",
					null
			);
			
			when(userService.existsByUsername(request.username())).thenReturn(false);
			when(userService.existsByEmail(request.email())).thenReturn(false);
			
			// Act & Assert
			assertThatThrownBy(() -> authService.register(request))
					.isInstanceOf(IllegalArgumentException.class);
		}
		
		@Test
		@DisplayName("При корректных данных должен зарегистрировать пользователя, сохранить его и вернуть DTO")
		void register_whenValidRequest_shouldSaveUserAndReturnDto() {
			// Arrange
			RegisterRequestDto request = new RegisterRequestDto(
					existingUser.getUsername(),
					existingUser.getEmail(),
					"rawPassword",
					true
			);
			
			UserResponseDto expectedResponse = new UserResponseDto(
					existingUser.getId(),
					existingUser.getUsername(),
					existingUser.getEmail(),
					existingUser.getRole().toString(),
					existingUser.getCreatedAt()
			);
			
			when(userService.existsByUsername(request.username())).thenReturn(false);
			when(userService.existsByEmail(request.email())).thenReturn(false);
			when(passwordEncoder.encode(request.password())).thenReturn(existingUser.getPasswordHash());
			when(userService.save(any(User.class))).thenReturn(existingUser);
			when(userMapper.toDto(existingUser)).thenReturn(expectedResponse);
			
			// Expected
			User expectedUserToSave = ClassesExamples.getUserToSave();
			
			// Act
			UserResponseDto actualResponse = authService.register(request);
			
			// Assert
			verify(userService).save(userCaptor.capture());
			User capturedUser = userCaptor.getValue();
			
			assertThat(capturedUser)
					.usingRecursiveComparison()
					.isEqualTo(expectedUserToSave);
			
			assertThat(actualResponse).isSameAs(expectedResponse);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода login")
	class LoginTests {
		
		@Test
		@DisplayName("При корректных учетных данных должен аутентифицировать пользователя и вернуть JWT токен")
		void login_whenValidCredentials_shouldAuthenticateAndReturnLoginResponseDto() {
			// Arrange
			LoginRequestDto request = new LoginRequestDto(existingUser.getUsername(), "password123");
			CustomUserDetails userDetails = new CustomUserDetails(
					existingUser.getId(),
					existingUser.getUsername(),
					existingUser.getPasswordHash(),
					Collections.singletonList(new SimpleGrantedAuthority(existingUser.getRole().name()))
			);
			String generatedToken = "jwt.token.value";
			
			when(userDetailsService.loadUserByUsername(request.username())).thenReturn(userDetails);
			when(jwtService.generateToken(userDetails)).thenReturn(generatedToken);
			
			// Act
			LoginResponseDto response = authService.login(request);
			
			// Assert
			verify(authenticationManager).authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password())
			);
			assertThat(response.accessToken()).isEqualTo(generatedToken);
		}
		
		@Test
		@DisplayName("Если учетные данные неверны, должен выбросить исключение аутентификации")
		void login_whenInvalidCredentials_shouldThrowException() {
			// Arrange
			LoginRequestDto request = new LoginRequestDto("wrongUser", "wrongPassword");
			
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
					.thenThrow(new BadCredentialsException("Неверный логин или пароль"));
			
			// Act & Assert
			assertThatThrownBy(() -> authService.login(request))
					.isInstanceOf(BadCredentialsException.class);
		}
	}
}