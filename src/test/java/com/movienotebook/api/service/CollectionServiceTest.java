package com.movienotebook.api.service;

import com.movienotebook.api.dto.collection.CollectionRequestDto;
import com.movienotebook.api.dto.collection.CollectionResponseDto;
import com.movienotebook.api.dto.collection.CollectionWithMoviesResponseDto;
import com.movienotebook.api.entity.Collection;
import com.movienotebook.api.entity.CollectionMovie;
import com.movienotebook.api.entity.Movie;
import com.movienotebook.api.entity.User;
import com.movienotebook.api.exception.CollectionAlreadyExistsException;
import com.movienotebook.api.exception.MovieAlreadyInCollectionException;
import com.movienotebook.api.exception.ResourceNotFoundException;
import com.movienotebook.api.mapper.CollectionMapper;
import com.movienotebook.api.repository.CollectionMovieRepository;
import com.movienotebook.api.repository.CollectionRepository;
import com.movienotebook.api.security.CustomUserDetails;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {
	
	@Mock
	private UserService userService;
	
	@Mock
	private MovieService movieService;
	
	@Mock
	private CollectionRepository collectionRepository;
	
	@Mock
	private CollectionMovieRepository collectionMovieRepository;
	
	@Mock
	private CollectionMapper collectionMapper;
	
	@InjectMocks
	private CollectionService collectionService;
	
	@Captor
	private ArgumentCaptor<Collection> collectionCaptor;
	
	private User existingUser;
	private CustomUserDetails currentUser;
	private Collection existingCollection;
	private Movie existingMovie;
	
	private CollectionResponseDto mockCollectionDto;
	private CollectionWithMoviesResponseDto mockCollectionWithMoviesDto;
	
	@BeforeEach
	void setUp() {
		existingUser = ClassesExamples.getExistingUser();
		existingMovie = ClassesExamples.getExistingMovie();
		
		currentUser = new CustomUserDetails(
				existingUser.getId(),
				existingUser.getUsername(),
				existingUser.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		
		existingCollection = new Collection();
		existingCollection.setId(10L);
		existingCollection.setName("Любимые фильмы");
		existingCollection.setDescription("Моя лучшая подборка");
		existingCollection.setPublic(true);
		existingCollection.setUser(existingUser);
		
		mockCollectionDto = new CollectionResponseDto(
				existingCollection.getId(),
				existingCollection.getName(),
				existingCollection.getDescription(),
				existingCollection.isPublic(),
				OffsetDateTime.now(),
				OffsetDateTime.now()
		);
		
		mockCollectionWithMoviesDto = new CollectionWithMoviesResponseDto(
				existingCollection.getId(),
				existingCollection.getName(),
				existingCollection.getDescription(),
				existingCollection.isPublic(),
				existingUser.getUsername(),
				Collections.emptyList(),
				OffsetDateTime.now(),
				OffsetDateTime.now()
		);
	}
	
	@Nested
	@DisplayName("Тесты метода getById")
	class GetByIdTests {
		
		@Test
		@DisplayName("Если коллекция существует и пользователь ее автор, должен вернуть DTO")
		void getById_whenCollectionExistsAndUserIsAuthor_shouldReturnDto() {
			// Arrange
			Long collectionId = existingCollection.getId();
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(collectionMapper.toDto(existingCollection)).thenReturn(mockCollectionDto);
			
			// Act
			CollectionResponseDto result = collectionService.getById(collectionId, currentUser);
			
			// Assert
			assertThat(result).isSameAs(mockCollectionDto);
		}
		
		@Test
		@DisplayName("Если коллекция не существует, должен выбросить ResourceNotFoundException")
		void getById_whenCollectionDoesNotExist_shouldThrowException() {
			// Arrange
			Long collectionId = 999L;
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> collectionService.getById(collectionId, currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
		}
		
		@Test
		@DisplayName("Если пользователь не автор коллекции, должен выбросить AccessDeniedException")
		void getById_whenUserIsNotAuthor_shouldThrowException() {
			// Arrange
			Long collectionId = existingCollection.getId();
			User anotherUser = ClassesExamples.getExistingUser();
			anotherUser.setId(999L);
			existingCollection.setUser(anotherUser); // Меняем владельца коллекции
			
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			
			// Act & Assert
			assertThatThrownBy(() -> collectionService.getById(collectionId, currentUser))
					.isInstanceOf(AccessDeniedException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getWithMoviesById")
	class GetWithMoviesByIdTests {
		
		@Test
		@DisplayName("Если коллекция существует и пользователь ее автор, должен вернуть DTO с фильмами")
		void getWithMoviesById_whenCollectionExists_shouldReturnDtoWithMovies() {
			// Arrange
			Long collectionId = existingCollection.getId();
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(collectionMapper.toWithMoviesDto(existingCollection)).thenReturn(mockCollectionWithMoviesDto);
			
			// Act
			CollectionWithMoviesResponseDto result = collectionService.getWithMoviesById(collectionId, currentUser);
			
			// Assert
			assertThat(result).isSameAs(mockCollectionWithMoviesDto);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода getByCurrentUser")
	class GetByCurrentUserTests {
		
		@Test
		@DisplayName("Если у пользователя есть коллекции, должен вернуть список DTO")
		void getByCurrentUser_whenCollectionsExist_shouldReturnListOfDtos() {
			// Arrange
			when(collectionRepository.findAllByUserId(currentUser.getId())).thenReturn(List.of(existingCollection));
			when(collectionMapper.toDto(existingCollection)).thenReturn(mockCollectionDto);
			
			// Act
			List<CollectionResponseDto> result = collectionService.getByCurrentUser(currentUser);
			
			// Assert
			assertThat(result)
					.hasSize(1)
					.element(0).isSameAs(mockCollectionDto);
		}
		
		@Test
		@DisplayName("Если у пользователя нет коллекций, должен вернуть пустой список")
		void getByCurrentUser_whenNoCollections_shouldReturnEmptyList() {
			// Arrange
			when(collectionRepository.findAllByUserId(currentUser.getId())).thenReturn(Collections.emptyList());
			
			// Act
			List<CollectionResponseDto> result = collectionService.getByCurrentUser(currentUser);
			
			// Assert
			assertThat(result).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Тесты метода create")
	class CreateTests {
		
		@Test
		@DisplayName("Если имя уникально, должен создать коллекцию, сохранить и вернуть DTO")
		void create_whenNameIsUnique_shouldCreateSaveAndReturnDto() {
			// Arrange
			CollectionRequestDto request = new CollectionRequestDto("Новая коллекция", "Описание", true);
			
			when(collectionRepository.existsByNameAndUserId(request.name(), currentUser.getId())).thenReturn(false);
			when(userService.getReferenceById(currentUser.getId())).thenReturn(existingUser);
			when(collectionRepository.save(any(Collection.class))).thenReturn(existingCollection);
			when(collectionMapper.toDto(existingCollection)).thenReturn(mockCollectionDto);
			
			// Expected
			Collection expectedCollection = new Collection();
			expectedCollection.setUser(existingUser);
			expectedCollection.setName(request.name());
			expectedCollection.setDescription(request.description());
			expectedCollection.setPublic(request.isPublic());
			
			// Act
			CollectionResponseDto result = collectionService.create(request, currentUser);
			
			// Assert
			verify(collectionRepository).save(collectionCaptor.capture());
			Collection savedCollection = collectionCaptor.getValue();
			
			assertThat(savedCollection)
					.usingRecursiveComparison()
					.ignoringFields("id", "collectionMovies", "createdAt", "updatedAt")
					.isEqualTo(expectedCollection);
			
			assertThat(result).isSameAs(mockCollectionDto);
		}
		
		@Test
		@DisplayName("Если коллекция с таким именем уже есть, должен выбросить CollectionAlreadyExistsException")
		void create_whenNameAlreadyExists_shouldThrowException() {
			// Arrange
			CollectionRequestDto request = new CollectionRequestDto("Существующая коллекция", "Описание", true);
			
			when(collectionRepository.existsByNameAndUserId(request.name(), currentUser.getId())).thenReturn(true);
			
			// Act & Assert
			assertThatThrownBy(() -> collectionService.create(request, currentUser))
					.isInstanceOf(CollectionAlreadyExistsException.class);
			
			verify(collectionRepository, never()).save(any());
		}
	}
	
	@Nested
	@DisplayName("Тесты метода delete")
	class DeleteTests {
		
		@Test
		@DisplayName("Если коллекция существует и принадлежит пользователю, должен удалить ее")
		void delete_whenCollectionExistsAndUserIsAuthor_shouldDeleteCollection() {
			// Arrange
			Long collectionId = existingCollection.getId();
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			
			// Act
			collectionService.delete(collectionId, currentUser);
			
			// Assert
			verify(collectionRepository).delete(existingCollection);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода addMovie")
	class AddMovieTests {
		
		@Test
		@DisplayName("Если фильма нет в коллекции, должен добавить связь (мутировать сущность)")
		void addMovie_whenMovieNotInCollection_shouldAddMovie() {
			// Arrange
			Long collectionId = existingCollection.getId();
			Long movieId = existingMovie.getId();
			
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(movieService.getEntityById(movieId)).thenReturn(existingMovie);
			when(collectionMovieRepository.existsByCollectionIdAndMovieId(collectionId, movieId)).thenReturn(false);
			
			// Act
			collectionService.addMovie(collectionId, movieId, currentUser);
			
			// Assert
			// Проверяем мутацию самой сущности, так как метод полагается на JPA Cascade / Dirty Checking
			assertThat(existingCollection.getCollectionMovies()).hasSize(1);
			
			CollectionMovie addedLink = existingCollection.getCollectionMovies().iterator().next();
			assertThat(addedLink.getCollection()).isSameAs(existingCollection);
			assertThat(addedLink.getMovie()).isSameAs(existingMovie);
		}
		
		@Test
		@DisplayName("Если фильм уже есть в коллекции, должен выбросить MovieAlreadyInCollectionException")
		void addMovie_whenMovieAlreadyInCollection_shouldThrowException() {
			// Arrange
			Long collectionId = existingCollection.getId();
			Long movieId = existingMovie.getId();
			
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(movieService.getEntityById(movieId)).thenReturn(existingMovie);
			when(collectionMovieRepository.existsByCollectionIdAndMovieId(collectionId, movieId)).thenReturn(true);
			
			// Act & Assert
			assertThatThrownBy(() -> collectionService.addMovie(collectionId, movieId, currentUser))
					.isInstanceOf(MovieAlreadyInCollectionException.class);
		}
	}
	
	@Nested
	@DisplayName("Тесты метода removeMovie")
	class RemoveMovieTests {
		
		@Test
		@DisplayName("Если фильм есть в коллекции, должен удалить связь (мутировать сущность)")
		void removeMovie_whenMovieInCollection_shouldRemoveLink() {
			// Arrange
			Long collectionId = existingCollection.getId();
			Long movieId = existingMovie.getId();
			
			CollectionMovie link = new CollectionMovie();
			link.setCollection(existingCollection);
			link.setMovie(existingMovie);
			existingCollection.addMovie(link); // Добавляем перед тестом
			
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(collectionMovieRepository.findByCollectionIdAndMovieId(collectionId, movieId))
					.thenReturn(Optional.of(link));
			
			// Act
			collectionService.removeMovie(collectionId, movieId, currentUser);
			
			// Assert
			assertThat(existingCollection.getCollectionMovies()).isEmpty();
			assertThat(link.getCollection()).isNull(); // Убеждаемся, что метод removeMovie в сущности сработал корректно
		}
		
		@Test
		@DisplayName("Если связи между фильмом и коллекцией нет, должен выбросить ResourceNotFoundException")
		void removeMovie_whenMovieNotInCollection_shouldThrowException() {
			// Arrange
			Long collectionId = existingCollection.getId();
			Long movieId = existingMovie.getId();
			
			when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(existingCollection));
			when(collectionMovieRepository.findByCollectionIdAndMovieId(collectionId, movieId))
					.thenReturn(Optional.empty());
			
			// Act & Assert
			assertThatThrownBy(() -> collectionService.removeMovie(collectionId, movieId, currentUser))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}
}