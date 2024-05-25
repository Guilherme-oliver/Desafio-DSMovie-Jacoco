package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService scoreService;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScoreRepository scoreRepository;

	private UserEntity mockUser;
	private MovieEntity mockMovie;
	private ScoreDTO mockScoreDTO;

	@BeforeEach
	public void setup() {
		mockUser = UserFactory.createUserEntity();

		mockMovie = MovieFactory.createMovieEntity();

		mockScoreDTO = ScoreFactory.createScoreDTO();
	}

	@Test
	public void saveScoreShouldReturnMovieDTO() {
		when(userService.authenticated()).thenReturn(mockUser);
		when(movieRepository.findById(any(Long.class))).thenReturn(Optional.of(mockMovie));
		when(scoreRepository.saveAndFlush(any())).thenAnswer(i -> i.getArguments()[0]);

		MovieDTO result = scoreService.saveScore(mockScoreDTO);

		assertNotNull(result);
		assertEquals(mockMovie.getId(), result.getId());
		assertEquals(mockMovie.getTitle(), result.getTitle());
		verify(scoreRepository, times(1)).saveAndFlush(any(ScoreEntity.class));
	}

	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		when(userService.authenticated()).thenReturn(mockUser);
		when(movieRepository.findById(any(Long.class))).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			scoreService.saveScore(mockScoreDTO);
		});

		assertEquals("Recurso não encontrado", exception.getMessage());
		verify(movieRepository, times(1)).findById(any(Long.class));
		verify(scoreRepository, never()).saveAndFlush(any(ScoreEntity.class));
	}
}