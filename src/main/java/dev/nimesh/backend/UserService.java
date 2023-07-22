package dev.nimesh.backend;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CachePut;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.* ;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
@Service
@CacheConfig(cacheNames = "users")
public class UserService {


    private final UserRepository userRepository;
    private final AmazonS3 amazonS3Client; // Inject the AmazonS3 client to interact with AWS S3

    private final ImageService imageService; // Inject the ImageService


    @Autowired
    public UserService(UserRepository userRepository, AmazonS3 amazonS3Client ,ImageService imageService ) {
        this.userRepository = userRepository;
        this.amazonS3Client = amazonS3Client;
        this.imageService = imageService;
    }


    @Cacheable(key = "'image:' + #email", cacheNames = "images")
    public byte[] getImageByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getAvatarUrl() != null) {
            // Use the ImageService to fetch the image data from cache or S3
            byte[] data =  imageService.getImageDataFromCache(email);
            if(data != null) return data;
            byte[] dataFromS3 = downloadImageFromS3(user.getAvatarUrl());
            imageService.cacheImageData(email , dataFromS3);
            return dataFromS3 ;
        } else {
            // If user or image URL is not found in MongoDB or S3, return null
            return null;
        }
    }


    // Method to download the image from AWS S3 using the provided URL
    private byte[] downloadImageFromS3(String imageUrl) {
        try {
            String BUCKET_NAME = "shelf-backend";
            S3Object s3Object = amazonS3Client.getObject(BUCKET_NAME, imageUrl);
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private byte[] convertFileToBytes(MultipartFile file) throws IOException {
        return file.getBytes();
    }

    public void uploadImageAndSaveToUser(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            user = new User();
            user.setEmail(userEmail);
        }
        String imageUrl = uploadImageToS3(file, userEmail);

        user.setAvatarUrl(imageUrl);
        userRepository.save(user);

        byte[] fileData;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert file to byte array.");
        }

        // Store the file data in Redis with the email as the key using ImageService
        imageService.cacheImageData(userEmail, fileData);
    }

    // Helper method to upload image to S3
    private String uploadImageToS3(MultipartFile file, String userEmail) {
        try {
            // Replace "YOUR_S3_BUCKET_NAME" with your actual S3 bucket name
            String bucketName = "shelf-backend";
            String fileName = generateFileName(file);
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
    private String generateFileName(MultipartFile file) {
        // Generate a unique file name for the uploaded image
        return UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
    }


    @CachePut(key = "#email")
    public User createUserWithImage(String name, String email, MultipartFile profileImage) {
        try{// Adding FileType and FileSize Checks
        // Limit the file size to 5MB (you can adjust the size as needed)
        if (profileImage.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the limit of 5MB.");
        }

        // Check if the file type is JPG
        if (!profileImage.getContentType().equals("image/jpeg")) {
            throw new IllegalArgumentException("Only JPG images are allowed.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        // Step 2: Save the user object to MongoDB to generate an ObjectId
        user = userRepository.save(user);

        // Step 3: Upload the profileImage to AWS S3
        String fileName = generateFileName(profileImage);
        String fileUrl = uploadImageToS3( profileImage , fileName);
        user.setAvatarUrl(fileUrl);
        // Save the updated user object to MongoDB
        user = userRepository.save(user);

        return user;
        }
        catch (Exception e) {
            // Handle the exception appropriately (e.g., log the error, throw custom exception)
            e.printStackTrace();
            throw new RuntimeException("Failed to upload profile image.");
        }

    }

    @Cacheable(key = "#email", cacheNames = "users")
    public User getUserByEmail(String email) {
        // Use the UserRepository to fetch the user by email
        return userRepository.findByEmail(email);
    }
    public User updateUserImageByEmail(String email, MultipartFile profileImage) {
        // Retrieve the user from MongoDB based on the email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Step 1: Generate a unique file name for the uploaded image
        String fileName = generateFileName(profileImage);

        // Step 2: Upload the profileImage to AWS S3
        String fileUrl = uploadImageToS3( profileImage , fileName);
        // Update the user's avatarUrl with the new S3 URL
        user.setAvatarUrl(fileUrl);
        // Save the updated user object to MongoDB
        user = userRepository.save(user);

        return user;
    }
    private void deleteImageFromS3(String fileName) {
        // Delete the image from S3 using the generated file name (object key)
        String bucketName = "shelf-backend";
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }
    public User deleteUserImageByEmail(String email) {
        // Retrieve the user from MongoDB based on the email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Delete the user's image from AWS S3
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            deleteImageFromS3(avatarUrl);
            // Clear the user's avatarUrl in the user object
            user.setAvatarUrl("");
            // Save the updated user object to MongoDB
            user = userRepository.save(user);
        }

        return user;
    }
}
