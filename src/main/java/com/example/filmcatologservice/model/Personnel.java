package com.example.filmcatologservice.model;

import lombok.Data;

@Data
public class Personnel {
    private Long id;
    private String position;
    private Person person;
}
