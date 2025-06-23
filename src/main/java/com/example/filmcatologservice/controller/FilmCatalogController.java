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
import java.util.Optional;

@RestController
@RequestMapping("/api/catalog")
@Slf4j
@Tag(name = "Film Catalog API", description = "Endpoints for searching and retrieving films")
public class FilmCatalogController {

    private final FilmCatalogService filmCatalogService;

    @Autowired
    public FilmCatalogController(FilmCatalogService filmCatalogService) {
        this.filmCatalogService = filmCatalogService;
    }

    @GetMapping("/films")
    @Operation(summary = "Search films", description = "Searches films based on various criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved films"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public List<Film> searchFilms(
            @Parameter(description = "Film title (partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Comma-separated list of genre names") @RequestParam(required = false) String genreNames,
            @Parameter(description = "Release year") @RequestParam(required = false) Integer releaseYear,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) Double minRating,
            @Parameter(description = "Original language") @RequestParam(required = false) String originalLanguage,
            @Parameter(description = "Sort field (releaseYear, rating, popularity)") @RequestParam(defaultValue = "releaseYear") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Searching films with criteria: title={}, genreNames={}, releaseYear={}, minRating={}, originalLanguage={}, sortBy={}, sortDirection={}",
                title, genreNames, releaseYear, minRating, originalLanguage, sortBy, sortDirection);
        List<Film> films = filmCatalogService.searchFilms(title, genreNames, releaseYear, minRating, originalLanguage, sortBy, sortDirection);
        log.debug("Found {} films", films.size());
        return films;
    }

    @GetMapping("/films/{id}")
    @Operation(summary = "Get film by ID", description = "Retrieves a film by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved film"),
            @ApiResponse(responseCode = "404", description = "Film not found")
    })
    public ResponseEntity<Film> getFilmById(@Parameter(description = "ID of the film") @PathVariable Long id) {
        log.info("Fetching film with ID: {}", id);
        Optional<Film> film = filmCatalogService.getFilmById(id);
        if (film.isPresent()) {
            log.debug("Found film: {}", film.get().getTitle());
            return ResponseEntity.ok(film.get());
        } else {
            log.warn("Film with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
}