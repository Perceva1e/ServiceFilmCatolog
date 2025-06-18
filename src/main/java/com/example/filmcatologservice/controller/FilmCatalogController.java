package com.example.filmcatologservice.controller;

import com.example.filmcatologservice.model.Film;
import com.example.filmcatologservice.service.FilmCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/catalog/films")
@Tag(name = "Film Catalog API", description = "API for browsing and searching films")
public class FilmCatalogController {

    private final FilmCatalogService filmCatalogService;

    @Autowired
    public FilmCatalogController(FilmCatalogService filmCatalogService) {
        this.filmCatalogService = filmCatalogService;
    }

    @Operation(summary = "Search and filter films", description = "Retrieves a list of films based on filters, sorting, and search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of films")
    })
    @GetMapping
    public ResponseEntity<List<Film>> searchFilms(
            @Parameter(description = "Search by film title (partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by genre IDs (comma-separated)") @RequestParam(required = false) List<Long> genreIds,
            @Parameter(description = "Filter by release year") @RequestParam(required = false) Integer releaseYear,
            @Parameter(description = "Filter by minimum rating") @RequestParam(required = false) Double minRating,
            @Parameter(description = "Filter by original language") @RequestParam(required = false) String originalLanguage,
            @Parameter(description = "Sort by field (releaseYear, rating, popularity)") @RequestParam(defaultValue = "releaseYear") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Searching films with title: {}, genres: {}, year: {}, minRating: {}, language: {}, sortBy: {}, sortDirection: {}",
                title, genreIds, releaseYear, minRating, originalLanguage, sortBy, sortDirection);

        List<Film> films = filmCatalogService.searchFilms(title, genreIds, releaseYear, minRating, originalLanguage, sortBy, sortDirection);
        log.debug("Retrieved {} films", films.size());
        return ResponseEntity.ok(films);
    }

    @Operation(summary = "Get film by ID", description = "Retrieves a film by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved film"),
            @ApiResponse(responseCode = "404", description = "Film not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@Parameter(description = "ID of the film") @PathVariable Long id) {
        log.info("Fetching film with ID: {}", id);
        return filmCatalogService.getFilmById(id)
                .map(film -> {
                    log.debug("Found film: {}", film.getTitle());
                    return ResponseEntity.ok(film);
                })
                .orElseGet(() -> {
                    log.warn("Film with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }
}