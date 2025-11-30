package com.mentalhealthforum.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void testRegister_WithIdToken_Success() throws Exception {
        User user = new User("test@example.com", "Test User", "12345", "http://example.com/pic.jpg");
        when(userService.registerUser("valid_id_token")).thenReturn(user);

        String requestBody = "{\"idToken\": \"valid_id_token\"}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    public void testRegister_WithAccessToken_Success() throws Exception {
        User user = new User("access@example.com", "Access User", "67890", "http://example.com/access_pic.jpg");
        when(userService.registerUserWithAccessToken("valid_access_token")).thenReturn(user);

        String requestBody = "{\"accessToken\": \"valid_access_token\"}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("access@example.com"))
                .andExpect(jsonPath("$.name").value("Access User"));
    }

    @Test
    public void testRegister_MissingTokens_BadRequest() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Token is required"));
    }

    @Test
    public void testRegister_InvalidToken_Unauthorized() throws Exception {
        when(userService.registerUser(anyString())).thenThrow(new IllegalArgumentException("Invalid token"));

        String requestBody = "{\"idToken\": \"invalid_token\"}";

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").value("Invalid Token: Invalid token"));
    }
}

