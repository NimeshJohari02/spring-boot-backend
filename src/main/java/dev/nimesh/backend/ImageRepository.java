package dev.nimesh.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, String> {

    // Custom query to find the image entity by email
    ImageEntity findByEmail(String email);
}
