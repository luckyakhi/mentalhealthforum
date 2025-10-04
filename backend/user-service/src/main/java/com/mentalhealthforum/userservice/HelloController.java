package com.mentalhealthforum.userservice;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // A record is a modern, concise way to create an immutable data carrier class in Java.
    public record Message(String text) {}

    @GetMapping("/api/hello")
    // @CrossOrigin is important! It allows our React app (running on a different port)
    // to call this backend endpoint without being blocked by browser security (CORS).
    @CrossOrigin(origins = "http://localhost:5173")
    public Message getHelloMessage() {
        return new Message("Hello from the Secure Backend!");
    }
}