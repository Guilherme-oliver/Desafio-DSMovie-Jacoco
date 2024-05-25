package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

	@InjectMocks
	private MovieService movieService;

	@Mock
	private MovieRepository movieRepository;

	private Long existingMovieId, nonExistingMovieId, dependentProductId;
	private MovieEntity movieEntity;
	private MovieDTO movieDTO;
	private Page<MovieEntity> movieEntityPage;
	private String existingTitle = "Existing Movie";

	@BeforeEach
	public void setUp() {
		existingMovieId = 1L;
		nonExistingMovieId = 2L;
		dependentProductId = 3L;

		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = MovieFactory.createMovieDTO();

		List<MovieEntity> movieEntities = Arrays.asList(new MovieEntity(1L, existingTitle, 0.0, 0, "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
		movieEntityPage = new PageImpl<>(movieEntities);

		Mockito.when(movieRepository.searchByTitle(Mockito.eq(existingTitle), Mockito.any(Pageable.class)))
				.thenReturn(movieEntityPage);

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		Mockito.when(movieRepository.save(any())).thenReturn(movieEntity);

		Mockito.when(movieRepository.getReferenceById(existingMovieId)).thenReturn(movieEntity);
		Mockito.when(movieRepository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(movieRepository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(dependentProductId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingMovieId)).thenReturn(false);

		Mockito.doNothing().when(movieRepository).deleteById(existingMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentProductId);
	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Pageable pageable = PageRequest.of(0, 12);
		Page<MovieDTO> result = movieService.findAll(existingTitle, pageable);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(1, result.getSize());
		Assertions.assertEquals(existingTitle, result.getContent().get(0).getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = movieService.findById(existingMovieId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.findById(nonExistingMovieId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = movieService.insert(movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), movieEntity.getId());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = movieService.update(existingMovieId, movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.update(nonExistingMovieId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			movieService.delete(existingMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.delete(nonExistingMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			movieService.delete(dependentProductId);
		});
	}
}