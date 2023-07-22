package dev.nimesh.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController

public class FileUploadController {

    private final UserService userService;

    @Autowired
    public FileUploadController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "HealthCheck Up and Running ";
    }

    @PostMapping("/create")
    public User createUserWithImage(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("profileImage") MultipartFile profileImage
    ) {
        return userService.createUserWithImage(name, email, profileImage);
    }
    @GetMapping("/fetch")
    public User fetchUserByEmail(@RequestParam("email") String email) {
        return userService.getUserByEmail(email);
    }
    @PostMapping("/updateImage")
    public User updateUserImageByEmail(
            @RequestParam("email") String email,
            @RequestParam("profileImage") MultipartFile profileImage
    ) {
        return userService.updateUserImageByEmail(email, profileImage);
    }
    @PostMapping("/delete")
    public User deleteUserImageByEmail(@RequestParam("email") String email) {
        return userService.deleteUserImageByEmail(email);
    }
}
