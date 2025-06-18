package com.example.filmcatologservice.model;
import lombok.Data;

import java.util.List;

@Data
public class User {
    private Long id;
    private String email;
    private String name;
    private String hashedPassword;
    private List<Review> reviews;
}