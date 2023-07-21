package dev.nimesh.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // Method to retrieve the image from the H2 database by email
    public byte[] getImageByEmail(String email) {
        ImageEntity imageEntity = imageRepository.findByEmail(email);
        return imageEntity != null ? imageEntity.getImageData() : null;
    }
}