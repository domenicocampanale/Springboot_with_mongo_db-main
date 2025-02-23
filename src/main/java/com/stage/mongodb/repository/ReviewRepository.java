package com.stage.mongodb.repository;

import com.stage.mongodb.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByMovieId(String movieId);

    void deleteByMovieId(String movieId);

}
