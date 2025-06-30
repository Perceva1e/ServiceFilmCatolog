package com.example.filmcatologservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class FilmSearchCriteria {
    private String title;
    private List<String> genreNames;
    private Integer releaseYear;
    private Double minRating;
    private String originalLanguage;
    private String sortBy;
    private String sortDirection;
}