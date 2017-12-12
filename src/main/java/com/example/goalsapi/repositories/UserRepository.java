package com.example.goalsapi.repositories;

import com.example.goalsapi.models.dao.UserDao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<UserDao, String> {

    boolean existsByEmailAddress(String emailAddress);

    @Query(value = "{'userId': ?0}", fields = "{'auth0Id': 1}")
    UserDao getAuth0IdByUserId(String userId);

    @Query(value = "{'userId':?0}", fields = "{'refreshToken':'1'}")
    UserDao getRefreshTokenByUserId(String userId);

}
