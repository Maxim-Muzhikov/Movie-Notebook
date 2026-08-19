package com.movienotebook.api.service;

import com.movienotebook.api.dto.user.UserResponseDto;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.UserMapper;
import com.movienotebook.api.repository.UserRepository;
import com.movienotebook.api.util.ClassesExamples;
import org.jetbrains.annotations.NotNull;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserMapper userMapper;
	
	@InjectMocks
	private UserService userService;
	
	@Captor
	private ArgumentCaptor<User> userCaptor;
	
	private User existingUser;
	
	@BeforeEach
	void setUp() {
		existingUser = ClassesExamples.getExistingUser();
	}
	
	@Nested
	@DisplayName("Тесты метода getByUsername")
	class GetByUsernameTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен отобразить его в DTO и вернуть")
		void getByUsername_whenUserExist_shouldReturnDtoOfExistingUser() {
			// Arrange
			String username = existingUser.getUsername();
			
			UserResponseDto mappedUser = mapUserToResponseDto(existingUser);
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
			when(userMapper.toDto(existingUser)).thenReturn(mappedUser);
			
			// Act
			UserResponseDto returnedUser = userService.getByUsername(username);
			
			// Assert
			assertThat(returnedUser).isSameAs(mappedUser);
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен выбросить исключение ResourceNotFound")
		void getByUsername_whenUserDoesNotExist_shouldThrowException () {
			// Arrange
			String username = "Несуществующий пользователь";
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
			
			//Act & Assert
			assertThatThrownBy(() -> userService.getByUsername(username))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен отобразить его в DTO и вернуть")
		void getById_whenUserExist_shouldReturnDtoOfExistingUser() {
			// Arrange
			Long id = existingUser.getId();
			
			UserResponseDto mappedUser = mapUserToResponseDto(existingUser);
			
			when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
			when(userMapper.toDto(existingUser)).thenReturn(mappedUser);
			
			// Act
			UserResponseDto returnedUser = userService.getById(id);
			
			//Assert
			assertThat(returnedUser).isSameAs(mappedUser);
		}
		
		@Test
		@DisplayName("Если пользователя не существует, должен выбросить исключение ResourceNotFound")
		void getById_whenUserDoesNotExist_shouldThrowException() {
			// Arrange
			Long id = 999L;
			
			when(userRepository.findById(id)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> userService.getById(id))
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
			String username = existingUser.getUsername();
			
			when(userRepository.existsByUsername(username)).thenReturn(true);
			
			// Act
			boolean returnedAnswer = userService.existsByUsername(username);
			
			// Assert
			assertThat(returnedAnswer).isTrue();
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен вернуть false")
		void existsByUsername_whenUserDoesNotExist_shouldReturnFalse() {
			// Arrange
			String username = "Несуществующий пользователь";
			
			when(userRepository.existsByUsername(username)).thenReturn(false);
			
			// Act
			boolean returnedAnswer = userService.existsByUsername(username);
			
			// Assert
			assertThat(returnedAnswer).isFalse();
		}
	
	}
	
	@Nested
	@DisplayName("Тесты методы existsByEmail")
	class ExistsByEmail {
		
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть true")
		void existsByUsername_whenUserExist_shouldReturnTrue() {
			// Arrange
			String email = existingUser.getEmail();
			
			when(userRepository.existsByEmail(email)).thenReturn(true);
			
			// Act
			boolean returnedAnswer = userService.existsByEmail(email);
			
			// Assert
			assertThat(returnedAnswer).isTrue();
		}
		
		@Test
		@DisplayName("Если пользователь не существует, должен вернуть false")
		void existsByUsername_whenUserDoesNotExist_shouldReturnFalse() {
			// Arrange
			String email = "Несуществующая почта";
			
			when(userRepository.existsByEmail(email)).thenReturn(false);
			
			// Act
			boolean returnedAnswer = userService.existsByEmail(email);
			
			// Assert
			assertThat(returnedAnswer).isFalse();
		}
	
	}
	
	@Nested
	@DisplayName("Тесты метода getEntityByUsername")
	class GetEntityByUsernameTests {
		
		@Test
		@DisplayName("Если пользователь существует, должен вернуть его сущность")
		void getEntityByUsername_whenUserExist_shouldReturnEntityOfExistingUser() {
			// Arrange
			String username = existingUser.getUsername();
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
			
			// Act
			User returnedUser = userService.getEntityByUsername(username);
			
			// Assert
			assertThat(returnedUser).isSameAs(existingUser);
		}
		
		@Test
		@DisplayName("Если пользователя не существует, должен выбросить исключение ResourceNotFound")
		void getEntityByUsername_whenUserDoesNotExist_shouldThrowException() {
			// Arrange
			String username = "Несуществующий пользователь";
			
			when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
			
			//Act & Assert
			assertThatThrownBy(() -> userService.getEntityByUsername(username))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	// getEntityById
	@Nested
	@DisplayName("Тесты метода getEntityById")
	class GetEntityByIdTests {
		@Test
		@DisplayName("Если пользователь существует, должен вернуть его сущность")
		void getEntityById_whenUserExists_shouldReturnEntityOfExistingUser() {
			// Arrange
			Long id = existingUser.getId();
			
			when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
			
			// Act
			User returnedUser = userService.getEntityById(id);
			
			// Assert
			assertThat(returnedUser).isSameAs(existingUser);
		}
		
		@Test
		@DisplayName("Если пользователя не существует, должен выбросить исключение ResourceNotFound")
		void getEntityById_whenUserDoesNotExist_shouldThrowException() {
			// Arrange
			Long id = existingUser.getId();
			
			when(userRepository.findById(id)).thenReturn(Optional.empty());
			
			//Act & Assert
			assertThatThrownBy(() -> userService.getEntityById(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getReferenceById")
	class GetReferenceByIdTests {
		
		@Test
		@DisplayName("Должен вернуть ссылку на пользователя")
		void getReferenceByIdTest_always_shouldReturnReferenceUser() {
			// Arrange
			Long id = existingUser.getId();
			
			when(userRepository.getReferenceById(id)).thenReturn(existingUser);
			
			// Act
			User returnedUser = userService.getReferenceById(id);
			
			// Assert
			assertThat(returnedUser)
					.isSameAs(existingUser);
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
			
			when(userRepository.save(userToSave)).thenReturn(existingUser);
			
			// Expected
			User expectedSavedUser = ClassesExamples.getUserToSave();
			User expectedReturnedUser = ClassesExamples.getExistingUser();
			
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
					.usingRecursiveComparison()
					.isEqualTo(expectedReturnedUser);
		}
		
	}
	
	private @NotNull UserResponseDto mapUserToResponseDto(User user) {
		return new UserResponseDto(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole().toString(),
				user.getCreatedAt()
		);
	}
}
