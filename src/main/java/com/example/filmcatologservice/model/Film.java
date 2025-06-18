package com.example.filmcatologservice.model;

import lombok.Data;

import java.util.List;

@Data
public class Film {
    private Long id;
    private String title;
    private int releaseYear;
    private String originalLanguage;
    private Integer duration;
    private Double rating;
    private FilmData filmData;
    private List<Review> reviews;
    private List<Genre> genres;
    private List<Personnel> personnel;
}