package com.mentalhealthforum.userservice;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final GoogleUserInfoClient googleUserInfoClient;

    public UserService(UserRepository userRepository, GoogleTokenVerifier googleTokenVerifier, GoogleUserInfoClient googleUserInfoClient) {
        this.userRepository = userRepository;
        this.googleTokenVerifier = googleTokenVerifier;
        this.googleUserInfoClient = googleUserInfoClient;
    }

    @Transactional
    public User registerUser(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idTokenString);

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        return saveOrUpdateUser(email, name, googleId, pictureUrl);
    }

    @Transactional
    public User registerUserWithAccessToken(String accessToken) throws IOException {
        // Verify access token by calling Google's userinfo endpoint
        java.util.Map<String, Object> userInfo = googleUserInfoClient.getUserInfo(accessToken);

        String googleId = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String pictureUrl = (String) userInfo.get("picture");

        return saveOrUpdateUser(email, name, googleId, pictureUrl);
    }

    private User saveOrUpdateUser(String email, String name, String googleId, String pictureUrl) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setProfilePictureUrl(pictureUrl);
            return userRepository.save(user);
        } else {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                user.setGoogleId(googleId);
                user.setName(name);
                user.setProfilePictureUrl(pictureUrl);
                return userRepository.save(user);
            }

            User newUser = new User(email, name, googleId, pictureUrl);
            return userRepository.save(newUser);
        }
    }
}
