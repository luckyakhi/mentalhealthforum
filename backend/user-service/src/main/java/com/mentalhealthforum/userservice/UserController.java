package com.mentalhealthforum.userservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        String accessToken = request.get("accessToken");

        if (idToken == null && accessToken == null) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        try {
            User user;
            if (idToken != null) {
                user = userService.registerUser(idToken);
            } else {
                user = userService.registerUserWithAccessToken(accessToken);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token: " + e.getMessage());
        }
    }
}
