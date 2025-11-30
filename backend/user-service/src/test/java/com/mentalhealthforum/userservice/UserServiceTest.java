package com.mentalhealthforum.userservice;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private GoogleUserInfoClient googleUserInfoClient;

    @InjectMocks
    private UserService userService;

    @Test
    public void testRegisterUser_NewUser() throws GeneralSecurityException, IOException {
        // Mock Google Token
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("12345");
        payload.setEmail("test@example.com");
        payload.set("name", "Test User");
        payload.set("picture", "http://example.com/pic.jpg");

        when(googleTokenVerifier.verify("valid_token")).thenReturn(payload);
        when(userRepository.findByGoogleId("12345")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        User savedUser = new User("test@example.com", "Test User", "12345", "http://example.com/pic.jpg");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser("valid_token");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_ExistingUser() throws GeneralSecurityException, IOException {
        // Mock Google Token
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("12345");
        payload.setEmail("test@example.com");
        payload.set("name", "Updated Name");
        payload.set("picture", "http://example.com/new_pic.jpg");

        when(googleTokenVerifier.verify("valid_token")).thenReturn(payload);

        User existingUser = new User("test@example.com", "Old Name", "12345", "http://example.com/old_pic.jpg");
        when(userRepository.findByGoogleId("12345")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.registerUser("valid_token");

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("http://example.com/new_pic.jpg", result.getProfilePictureUrl());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void testRegisterUserWithAccessToken_NewUser() throws IOException {
        // Mock User Info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", "67890");
        userInfo.put("email", "access@example.com");
        userInfo.put("name", "Access User");
        userInfo.put("picture", "http://example.com/access_pic.jpg");

        when(googleUserInfoClient.getUserInfo("valid_access_token")).thenReturn(userInfo);
        when(userRepository.findByGoogleId("67890")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("access@example.com")).thenReturn(Optional.empty());

        User savedUser = new User("access@example.com", "Access User", "67890", "http://example.com/access_pic.jpg");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUserWithAccessToken("valid_access_token");

        assertNotNull(result);
        assertEquals("access@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithAccessToken_ExistingUser() throws IOException {
        // Mock User Info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", "67890");
        userInfo.put("email", "access@example.com");
        userInfo.put("name", "Updated Access User");
        userInfo.put("picture", "http://example.com/new_access_pic.jpg");

        when(googleUserInfoClient.getUserInfo("valid_access_token")).thenReturn(userInfo);

        User existingUser = new User("access@example.com", "Old Access User", "67890", "http://example.com/old_access_pic.jpg");
        when(userRepository.findByGoogleId("67890")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.registerUserWithAccessToken("valid_access_token");

        assertNotNull(result);
        assertEquals("Updated Access User", result.getName());
        assertEquals("http://example.com/new_access_pic.jpg", result.getProfilePictureUrl());
        verify(userRepository).save(existingUser);
    }
}
