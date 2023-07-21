package dev.nimesh.backend;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByName(String name);

    // Custom query to find users by email
    User findByEmail(String email);

}
