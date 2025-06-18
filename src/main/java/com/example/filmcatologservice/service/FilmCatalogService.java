package com.example.filmcatologservice.service;

import com.example.filmcatologservice.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmCatalogService {

    private final RestTemplate restTemplate;

    @Autowired
    public FilmCatalogService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Film> searchFilms(String title, List<Long> genreIds, Integer releaseYear, Double minRating,
                                  String originalLanguage, String sortBy, String sortDirection) {
        log.info("Fetching films from servicedb");
        Film[] filmsArray = restTemplate.getForObject("/films", Film[].class);
        List<Film> films = Arrays.asList(filmsArray != null ? filmsArray : new Film[0]);

        films = applyFilters(films, title, genreIds, releaseYear, minRating, originalLanguage);

        films = applySorting(films, sortBy, sortDirection);

        return films;
    }

    public Optional<Film> getFilmById(Long id) {
        log.info("Fetching film with ID: {} from servicedb", id);
        try {
            Film film = restTemplate.getForObject("/films/" + id, Film.class);
            return Optional.ofNullable(film);
        } catch (Exception e) {
            log.error("Failed to retrieve film with ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    private List<Film> applyFilters(List<Film> films, String title, List<Long> genreIds, Integer releaseYear,
                                    Double minRating, String originalLanguage) {
        return films.stream()
                .filter(film -> title == null || film.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(film -> genreIds == null || genreIds.isEmpty() || film.getGenres().stream()
                        .anyMatch(genre -> genreIds.contains(genre.getId())))
                .filter(film -> releaseYear == null || film.getReleaseYear() == releaseYear)
                .filter(film -> minRating == null || (film.getRating() != null && film.getRating() >= minRating))
                .filter(film -> originalLanguage == null || Objects.equals(film.getOriginalLanguage(), originalLanguage))
                .collect(Collectors.toList());
    }

    private List<Film> applySorting(List<Film> films, String sortBy, String sortDirection) {
        Comparator<Film> comparator;
        switch (sortBy.toLowerCase()) {
            case "rating":
                comparator = Comparator.comparing(film -> film.getRating() != null ? film.getRating() : 0.0, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "popularity":
                comparator = Comparator.comparing(film -> calculatePopularity(film), Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "releaseyear":
            default:
                comparator = Comparator.comparingInt(Film::getReleaseYear);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return films.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private double calculatePopularity(Film film) {
        if (film.getReviews() == null || film.getReviews().isEmpty()) {
            return 0.0;
        }
        return film.getReviews().stream()
                .mapToDouble(review -> review.getNumberOfLikes() - review.getNumberOfDislikes())
                .sum();
    }
}