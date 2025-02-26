package com.stage.mongodb.repository;

import com.stage.mongodb.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    void deleteByMovieId(String movieId);

}
