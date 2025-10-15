package api.savingstracker.authentication_service.aws;

import api.savingstracker.authentication_service.annotations.PathParameter;
import api.savingstracker.authentication_service.annotations.RequestBody;
import api.savingstracker.authentication_service.annotations.RequestEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiGatewayHandler {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ApiResponse apply(ApiGatewayEvent event) {
    Method invokeMethod = findInvokeMethod();

    try {
      Parameter[] parameters = invokeMethod.getParameters();
      Object[] arguments = new Object[parameters.length];

      for (int i = 0; i < parameters.length; i++) {
        Parameter param = parameters[i];
        if (param.isAnnotationPresent(RequestBody.class)) {
          String body = event.getBody();
          if (body == null || body.trim().isEmpty()) {
            arguments[i] = null;
            continue;
          }

          Map<String, String> headers = event.getHeaders() != null ? event.getHeaders() : Collections.emptyMap();
          String contentType = headers.getOrDefault("content-type", headers.get("Content-Type")); // Case-insensitive

          if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            Map<String, String> formData = parseUrlEncodedBody(body);
            arguments[i] = objectMapper.convertValue(formData, param.getType());
          } else {
            arguments[i] = objectMapper.readValue(body, param.getType());
          }
        } else if (param.isAnnotationPresent(PathParameter.class)) {
          String paramName = param.getAnnotation(PathParameter.class).value();
          String paramValue = event.getPathParameters().get(paramName);
          arguments[i] = convertType(paramValue, param.getType());
        } else if (param.isAnnotationPresent(RequestEvent.class)) {
          if (param.getType().equals(ApiGatewayEvent.class)) arguments[i] = event;
          else throw new IllegalStateException("Parameter with @RequestEvent must be of type APIGatewayProxyRequestEvent.");
        }
      }

      return (ApiResponse) invokeMethod.invoke(this, arguments);
    } catch (Exception exception) {
      System.err.println("Unhandled exception in handler: " + getRootCause(exception).getMessage());
      return new ApiResponseBuilder(objectMapper)
          .withStatusCode(500)
          .withJsonBody(Map.of("error", "Internal Server Error."))
          .build();
    }
  }

  protected ApiResponseBuilder createResponse(int statusCode, Object body) {
    try {
      return new ApiResponseBuilder()
          .withStatusCode(statusCode)
          .withJsonBody(body);
    } catch (Exception e) {
      return new ApiResponseBuilder()
          .withStatusCode(500)
          .withJsonBody(Map.of("error", "Failed to serialize response body."));
    }
  }

  private Map<String, String> parseUrlEncodedBody(String body) {
    Map<String, String> map = new HashMap<>();
    if (body == null || body.isEmpty()) return map;
    String[] pairs = body.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      if (idx == -1) continue;
      String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
      String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
      map.put(key, value);
    }
    return map;
  }

  private Method findInvokeMethod() {
    return Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().equals("invoke")).findFirst()
        .orElseThrow(() -> new IllegalStateException("No 'invoke' method found in handler class " + this.getClass().getName()));
  }

  private Object convertType(String value, Class<?> targetType) {
    if (value == null)
      return null;
    if (targetType.equals(String.class))
      return value;
    if (targetType.equals(Integer.class) || targetType.equals(int.class))
      return Integer.parseInt(value);
    if (targetType.equals(Long.class) || targetType.equals(long.class))
      return Long.parseLong(value);
    throw new IllegalArgumentException("Unsupported parameter type for conversion: " + targetType.getName());
  }

  private Throwable getRootCause(Throwable throwable) {
    if (throwable instanceof InvocationTargetException)
      return throwable.getCause();
    return throwable;
  }
}
