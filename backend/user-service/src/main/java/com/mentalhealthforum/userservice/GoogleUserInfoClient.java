package com.mentalhealthforum.userservice;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class GoogleUserInfoClient {

    public Map<String, Object> getUserInfo(String accessToken) throws IOException {
        com.google.api.client.http.HttpRequestFactory requestFactory = new com.google.api.client.http.javanet.NetHttpTransport()
                .createRequestFactory();
        com.google.api.client.http.GenericUrl url = new com.google.api.client.http.GenericUrl(
                "https://www.googleapis.com/oauth2/v3/userinfo");
        com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAuthorization("Bearer " + accessToken);

        com.google.api.client.http.HttpResponse response = request.execute();
        com.google.api.client.json.gson.GsonFactory gsonFactory = new com.google.api.client.json.gson.GsonFactory();
        com.google.api.client.json.JsonParser parser = gsonFactory.createJsonParser(response.getContent());

        // Parse response
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) parser.parse(Map.class);
        return map;
    }
}

