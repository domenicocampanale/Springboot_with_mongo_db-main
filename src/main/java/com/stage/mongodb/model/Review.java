package com.stage.mongodb.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "Review")
@Builder
@Getter
@Setter
public class Review {

    @Id
    private String id;
    private String movieId;
    private int rating;
    private String comment;
    public Instant insertDate;
    public Instant updateDate;


}
