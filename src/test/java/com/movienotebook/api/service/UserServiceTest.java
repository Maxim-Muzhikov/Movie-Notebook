package com.movienotebook.api.service;

import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.repository.UserRepository;
import com.movienotebook.api.util.ClassesExamples;
import io.jsonwebtoken.lang.Classes;
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

import javax.swing.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private UserService userService;
	
	@Captor
	private ArgumentCaptor<User> userCaptor;
	
	private User testUser;
	private User expectedUser;
	
	@BeforeEach
	void setUp() {
		testUser = ClassesExamples.getExistingUser();
		expectedUser = ClassesExamples.getExistingUser();
		
	}
	
	@Nested
	@DisplayName("Тесты метода getByUsername")
	class GetByUsernameTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть существующего пользователя")
		void getByUsername_whenUserExist_shouldReturnExistingUser () {
			// Arrange
			String username = testUser.getUsername();
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
			
			// Act
			User returnedUser = userService.getByUsername(username);
			
			//Assert
			assertThat(returnedUser)
					.usingRecursiveComparison()
					.isEqualTo(expectedUser);
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен выбросить исключение ResourceNotFound")
		void getByUsername_whenUserDoesNotExist_shouldThrowException () {
			// Arrange
			String username = testUser.getUsername();
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
			
			//Act & Assert
			assertThatThrownBy(() -> userService.getByUsername(username))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода existsByEmail")
	class ExistsByUsernameTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть true")
		void existsByUsername_whenUserExist_shouldReturnTrue() {
			// Arrange
			String username = testUser.getUsername();
			
			when(userRepository.existsByUsername(username)).thenReturn(true);
			
			// Act
			boolean returnedAnswer = userService.existsByUsername(username);
			
			// Assert
			assertThat(returnedAnswer)
					.isTrue();
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен вернуть false")
		void existsByUsername_whenUserDoesNotExist_shouldReturnFalse() {
			// Arrange
			String username = testUser.getUsername();
			
			when(userRepository.existsByUsername(username)).thenReturn(false);
			
			// Act
			boolean returnedAnswer = userService.existsByUsername(username);
			
			// Assert
			assertThat(returnedAnswer)
					.isFalse();
		}
	}
	
	@Nested
	@DisplayName("Тесты методы existsByEmail")
	class ExistsByEmail {
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть true")
		void existsByUsername_whenUserExist_shouldReturnTrue() {
			// Arrange
			String email = testUser.getEmail();
			
			when(userRepository.existsByEmail(email)).thenReturn(true);
			
			// Act
			boolean returnedAnswer = userService.existsByEmail(email);
			
			// Assert
			assertThat(returnedAnswer)
					.isTrue();
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен вернуть false")
		void existsByUsername_whenUserDoesNotExist_shouldReturnFalse() {
			// Arrange
			String email = testUser.getEmail();
			
			when(userRepository.existsByEmail(email)).thenReturn(false);
			
			// Act
			boolean returnedAnswer = userService.existsByEmail(email);
			
			// Assert
			assertThat(returnedAnswer)
					.isFalse();
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть существующего пользователя")
		void getById_whenUserExist_shouldReturnExistingUser() {
			// Arrange
			Long id = testUser.getId();
			
			when(userRepository.findById(id)).thenReturn(Optional.of(testUser));
			
			// Act
			User returnedUser = userService.getById(id);
			
			//Assert
			assertThat(returnedUser)
					.usingRecursiveComparison()
					.isEqualTo(expectedUser);
		}
		
		@Test
		@DisplayName("Если пользователя не существует, должен выбросить исключение ResourceNotFound")
		void getById_whenUserDoesNotExist_shouldThrowException() {
			// Arrange
			Long id = testUser.getId();
			
			when(userRepository.findById(id)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> userService.getById(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода save")
	class SaveTests {
		
		@Test
		@DisplayName("Должен сохранить переданного пользователя")
		void save_shouldSaveUser() {
			// Arrange
			User userToSave = ClassesExamples.getUserToSave();
			
			when(userRepository.save(userToSave)).thenReturn(testUser);
			
			// Expected
			User expectedSavedUser = ClassesExamples.getUserToSave();
			
			// Act
			User returnedUser = userService.save(userToSave);
			
			// Assert
			verify(userRepository, times(1)).save(userCaptor.capture());
			User savedUser = userCaptor.getValue();
			
			assertThat(savedUser)
					.isSameAs(userToSave)
					.usingRecursiveComparison()
					.isEqualTo(expectedSavedUser);
			
			assertThat(returnedUser)
					.isSameAs(testUser)
					.usingRecursiveComparison()
					.isEqualTo(expectedUser);
		}
	}

	@Nested
	@DisplayName("Тесты метода getReferenceById")
	class GetReferenceByIdTests {
		
		@Test
		@DisplayName("Должен вернуть reference пользователя")
		void getReferenceByIdTest_always_shouldReturnReferenceUser() {
			// Arrange
			Long id = testUser.getId();
			
			when(userRepository.getReferenceById(id)).thenReturn(testUser);
			
			// Act
			User returnedUser = userService.getReferenceById(id);
			
			// Assert
			assertThat(returnedUser)
					.usingRecursiveComparison()
					.isEqualTo(expectedUser);
		}
	}
	
	
}
