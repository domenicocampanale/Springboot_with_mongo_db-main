package com.stage.mongodb.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "Movie")
@Builder
@Getter
@Setter
public class Movie {

    @Id
    private String id;
    private String title;
    private String releaseDate;
    public Instant insertDate;
    public Instant updateDate;


}
