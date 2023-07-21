package dev.nimesh.backend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final UserService userService;

    @Autowired
    public ImageController(UserService userService) {
        this.userService =  userService;
    }

    // Endpoint to upload an image to S3 for a given email ID
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
        try {
            userService.uploadImageAndSaveToUser(file, email);
            return new ResponseEntity<>("Image uploaded successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error uploading image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to fetch an image from S3 for a given email ID
    @GetMapping("/fetch")
    public ResponseEntity<byte[]> fetchImage(@RequestParam("email") String email) {
        try {
            byte[] imageData = userService.getImageByEmail(email);
            if (imageData != null) {
                return new ResponseEntity<>(imageData, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
