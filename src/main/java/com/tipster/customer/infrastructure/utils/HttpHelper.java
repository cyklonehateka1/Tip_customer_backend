package com.tipster.customer.infrastructure.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
public class HttpHelper {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String post(String url, Object data, Map<String, String> headers) {
        try {
            String json = objectMapper.writeValueAsString(data);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));

            if (headers != null) {
                headers.forEach(builder::header);
            }

            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("POST request failed", e);
        }
    }

    public static String put(String url, Object data, Map<String, String> headers) {
        try {
            String json = objectMapper.writeValueAsString(data);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json));

            if (headers != null) {
                headers.forEach(builder::header);
            }

            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("POST request failed", e);
        }
    }

    public static String get(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();

            if (headers != null) {
                headers.forEach(builder::header);
            }

            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("GET request failed", e);
        }
    }

    public static String postWithBearerToken(String url, Object data, String token, Map<String, String> headers) {
        headers = ensureHeaderMap(headers);
        headers.put("Authorization", "Bearer " + token);
        return post(url, data, headers);
    }

    public static String putWithBearerToken(String url, Object data, String token, Map<String, String> headers) {
        headers = ensureHeaderMap(headers);
        headers.put("Authorization", "Bearer " + token);
        return put(url, data, headers);
    }

    public static String getWithBearerToken(String url, String token, Map<String, String> headers) {
        headers = ensureHeaderMap(headers);
        headers.put("Authorization", "Bearer " + token);
        return get(url, headers);
    }

    public static String postWithBasicAuth(String url, Object data, String username, String password, Map<String, String> headers) {
        headers = ensureHeaderMap(headers);
        headers.put("Authorization", "Basic " + encodeBasicAuth(username, password));
        return post(url, data, headers);
    }

    public static String getWithBasicAuth(String url, String username, String password, Map<String, String> headers) {
        headers = ensureHeaderMap(headers);
        headers.put("Authorization", "Basic " + encodeBasicAuth(username, password));
        return get(url, headers);
    }

    public static String postFormUrlEncoded(String url, List<Map.Entry<String, String>> data) {
        try {
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : data) {
                sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(sj.toString()))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("POST Form-UrlEncoded failed", e);
        }
    }

    private static String encodeBasicAuth(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> ensureHeaderMap(Map<String, String> headers) {
        return headers != null ? headers : new java.util.HashMap<>();
    }
}
