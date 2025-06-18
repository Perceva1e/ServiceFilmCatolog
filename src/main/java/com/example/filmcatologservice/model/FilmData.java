package com.example.filmcatologservice.model;

import lombok.Data;

@Data
public class FilmData {
    private Long id;
    private double rating;
    private double budget;
    private String poster;
    private String trailer;
    private double revenue;
}
