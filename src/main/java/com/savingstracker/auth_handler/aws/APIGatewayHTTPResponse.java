package com.savingstracker.auth_handler.aws;

import java.util.Arrays;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIGatewayHTTPResponse extends APIGatewayV2HTTPResponse {
  private final APIGatewayV2HTTPResponseBuilder delegate;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private APIGatewayHTTPResponse(APIGatewayV2HTTPResponseBuilder delegate) {
    this.delegate = delegate;
  }

  public static APIGatewayHTTPResponse ok() {
    return status(200);
  }

  public static APIGatewayHTTPResponse status(int statusCode) {
    APIGatewayV2HTTPResponseBuilder builder = APIGatewayV2HTTPResponse.builder()
        .withStatusCode(statusCode);
    return new APIGatewayHTTPResponse(builder);
  }

  public APIGatewayHTTPResponse withCookies(String... cookies) {
    if (cookies != null && cookies.length > 0)
      this.delegate.withCookies(Arrays.asList(cookies));
    return this;
  }

  public APIGatewayHTTPResponse withJsonBody(Object body) {
    try {
      String jsonBody = objectMapper.writeValueAsString(body);
      this.delegate.withBody(jsonBody);
      this.delegate.withHeaders(Map.of("Content-Type", "application/json"));
    } catch (JsonProcessingException e) {
      this.delegate.withBody("{\"error\": \"Internal Server Error.\"}");
      this.delegate.withHeaders(Map.of("Content-Type", "application/json"));
    }
    return this;
  }

  public APIGatewayHTTPResponse withHeaders(Map<String, String> headers) {
    this.delegate.withHeaders(headers);
    return this;
  }

  public APIGatewayV2HTTPResponse build() {
    return this.delegate.build();
  }
}
