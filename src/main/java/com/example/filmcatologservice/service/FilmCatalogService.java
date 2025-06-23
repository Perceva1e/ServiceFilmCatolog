package com.example.filmcatologservice.service;

import com.example.filmcatologservice.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmCatalogService {

    private final RestTemplate restTemplate;
    private final String serviceDbApiUrl;

    @Autowired
    public FilmCatalogService(RestTemplate restTemplate, @Value("${servicedb.api.url}") String serviceDbApiUrl) {
        this.restTemplate = restTemplate;
        this.serviceDbApiUrl = serviceDbApiUrl;
    }

    public List<Film> searchFilms(String title, String genreNames, Integer releaseYear, Double minRating,
                                  String originalLanguage, String sortBy, String sortDirection) {
        log.info("Fetching films from servicedb at {}", serviceDbApiUrl + "/films");
        Film[] filmsArray = restTemplate.getForObject(serviceDbApiUrl + "/films", Film[].class);
        List<Film> films = Arrays.asList(filmsArray != null ? filmsArray : new Film[0]);

        List<String> genreNameList = genreNames != null ?
                Arrays.asList(genreNames.split(",\\s*")) : null;

        films = applyFilters(films, title, genreNameList, releaseYear, minRating, originalLanguage);

        films = applySorting(films, sortBy, sortDirection);

        log.debug("Returning {} films after filtering and sorting", films.size());
        return films;
    }

    public Optional<Film> getFilmById(Long id) {
        log.info("Fetching film with ID: {} from servicedb", id);
        try {
            Film film = restTemplate.getForObject(serviceDbApiUrl + "/films/" + id, Film.class);
            return Optional.ofNullable(film);
        } catch (Exception e) {
            log.error("Failed to retrieve film with ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    private List<Film> applyFilters(List<Film> films, String title, List<String> genreNames, Integer releaseYear,
                                    Double minRating, String originalLanguage) {
        return films.stream()
                .filter(film -> title == null || film.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(film -> genreNames == null || genreNames.isEmpty() || film.getGenres().stream()
                        .anyMatch(genre -> genreNames.stream()
                                .anyMatch(name -> genre.getName().toLowerCase().equals(name.toLowerCase()))))
                .filter(film -> releaseYear == null || film.getReleaseYear() == releaseYear)
                .filter(film -> minRating == null || (film.getRating() != null && film.getRating() >= minRating))
                .filter(film -> originalLanguage == null ||
                        (film.getOriginalLanguage() != null && film.getOriginalLanguage().toLowerCase().equals(originalLanguage.toLowerCase())))
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