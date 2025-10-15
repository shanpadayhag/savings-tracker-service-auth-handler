package api.savingstracker.authentication_service.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApiResponseBuilder {
  private final ObjectMapper objectMapper;
  private final ApiResponse response;

  public ApiResponseBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.response = new ApiResponse();
    this.response.setStatusCode(200);
    this.response.setHeaders(new HashMap<>(Map.of("Content-Type", "application/json")));
    this.response.setCookies(new ArrayList<>());
  }

  public ApiResponseBuilder() {
    this.objectMapper = new ObjectMapper();
    this.response = new ApiResponse();
    this.response.setStatusCode(200);
    this.response.setHeaders(new HashMap<>(Map.of("Content-Type", "application/json")));
    this.response.setCookies(new ArrayList<>());
  }

  public ApiResponseBuilder withStatusCode(int statusCode) {
    this.response.setStatusCode(statusCode);
    return this;
  }

  public ApiResponseBuilder withHeader(String key, String value) {
    this.response.getHeaders().put(key, value);
    return this;
  }

  public ApiResponseBuilder withHeaders(Map<String, String> headers) {
    this.response.getHeaders().putAll(headers);
    return this;
  }

  public ApiResponseBuilder withJsonBody(Object bodyObject) {
    try {
      this.response.setBody(objectMapper.writeValueAsString(bodyObject));
    } catch (JsonProcessingException e) {
      this.response.setStatusCode(500);
      this.response.setBody("{\"error\":\"Internal server error during JSON serialization\"}");
    }
    return this;
  }

  public ApiResponseBuilder withCookie(String cookie) {
    this.response.getCookies().add(cookie);
    return this;
  }

  public ApiResponse build() {
    return this.response;
  }
}
