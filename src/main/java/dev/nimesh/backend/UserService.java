package dev.nimesh.backend;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
@Service
public class UserService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3Client; // Inject the AmazonS3 client to interact with AWS S3

    @Autowired
    public UserService(ImageRepository imageRepository, UserRepository userRepository, AmazonS3 amazonS3Client) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.amazonS3Client = amazonS3Client;
    }

    // Method to retrieve the image from H2 if available, otherwise from AWS S3
    @Cacheable("userImages") // Cache the images with the key "userImages"
    public byte[] getImageByEmail(String email) {
        ImageEntity imageEntity = imageRepository.findByEmail(email);
        if (imageEntity != null) {
            // If image is found in H2, return the image data
            return imageEntity.getImageData();
        } else {
            // If image is not in H2, try to fetch it from AWS S3
            User user = userRepository.findByEmail(email);
            if (user != null && user.getAvatarUrl() != null) {
                byte[] imageData = downloadImageFromS3(user.getAvatarUrl());
                if (imageData != null) {
                    // If image is downloaded from S3, save it in H2 for future use
                    imageEntity = new ImageEntity(email, imageData);
                    imageRepository.save(imageEntity);
                }
                return imageData;
            } else {
                // If user or image URL is not found in MongoDB, return null
                return null;
            }
        }
    }

    // Method to download the image from AWS S3 using the provided URL
    private byte[] downloadImageFromS3(String imageUrl) {
        try {
            String BUCKET_NAME = "shelf";
            S3Object s3Object = amazonS3Client.getObject(BUCKET_NAME, imageUrl);
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadImageAndSaveToUser(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            user = new User();
            user.setEmail(userEmail);
        }
        String imageUrl = uploadImageToS3(file, userEmail);

        // Save the image URL to the user in MongoDB
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
    }

    // Helper method to upload image to S3
    private String uploadImageToS3(MultipartFile file, String userEmail) {
        try {
            // Replace "YOUR_S3_BUCKET_NAME" with your actual S3 bucket name
            String bucketName = "YOUR_S3_BUCKET_NAME";
            String fileName = userEmail + "/" + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

            // Get the public URL of the uploaded image
            return amazonS3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
