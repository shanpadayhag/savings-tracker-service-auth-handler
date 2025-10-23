package com.savingstracker.auth_handler.aws;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savingstracker.auth_handler.annotations.PathParameter;
import com.savingstracker.auth_handler.annotations.RequestBody;
import com.savingstracker.auth_handler.annotations.RequestEvent;

public class APIGatewayHandler implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Method invokeMethod;
  private final List<HandlerParameter> handlerParameters;

  private enum ParamType {
    REQUEST_BODY, PATH_PARAMETER, REQUEST_EVENT, UNKNOWN
  }

  private record HandlerParameter(int index, ParamType type, Class<?> parameterType, String name) {
  }

  public APIGatewayHandler() {
    this.invokeMethod = findInvokeMethod();
    this.handlerParameters = analyzeParameters(this.invokeMethod);
  }

  /**
   * This is the main entry point for the Lambda function. It uses the
   * pre-computed metadata to efficiently construct the arguments for the
   * {@code invoke} method, calls it, and returns the resulting HTTP response.
   *
   * @param event The {@link APIGatewayV2HTTPEvent} provided by AWS Lambda.
   * @return A {@link APIGatewayV2HTTPResponse} to be sent back to the client.
   */
  @Override
  public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
    try {
      Object[] arguments = prepareArguments(event);
      return (APIGatewayV2HTTPResponse) invokeMethod.invoke(this, arguments);
    } catch (Exception exception) {
      Throwable rootCause = getRootCause(exception);
      System.err.println("Unhandled exception in handler: " + rootCause.getMessage());
      return createErrorResponse(500, "Internal Server Error");
    }
  }

  /**
   * Constructs the argument array for the {@code invoke} method based on the
   * incoming event and the cached parameter metadata.
   *
   * @param event The incoming event.
   * @return An array of {@link Object}s ready to be passed to
   *         {@code Method.invoke()}.
   * @throws JsonProcessingException if the request body is JSON and cannot be
   *                                 parsed.
   */
  private Object[] prepareArguments(APIGatewayV2HTTPEvent event) throws JsonProcessingException {
    Object[] arguments = new Object[handlerParameters.size()];
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (event.getHeaders() != null) {
      headers.putAll(event.getHeaders());
    }

    for (HandlerParameter param : handlerParameters) {
      arguments[param.index] = switch (param.type) {
        case REQUEST_BODY -> parseBody(event.getBody(), headers.get("content-type"), param.parameterType);
        case PATH_PARAMETER -> {
          String paramValue = event.getPathParameters().get(param.name);
          yield convertType(paramValue, param.parameterType);
        }
        case REQUEST_EVENT -> event;
        case UNKNOWN -> throw new IllegalStateException("Unsupported parameter type at index " + param.index);
      };
    }
    return arguments;
  }

  /**
   * Deserializes the HTTP request body into a Java object based on the
   * Content-Type header. Supports {@code application/json} and
   * {@code application/x-www-form-urlencoded}. Defaults to JSON if the content
   * type is unrecognized.
   *
   * @param body        The raw request body string.
   * @param contentType The value of the Content-Type header.
   * @param targetType  The Java class to deserialize the body into.
   * @return The deserialized Java object, or null if the body is empty.
   * @throws JsonProcessingException if JSON deserialization fails.
   */
  private Object parseBody(String body, String contentType, Class<?> targetType) throws JsonProcessingException {
    if (body == null || body.isBlank()) return null;

    if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
      Map<String, String> formData = parseUrlEncodedBody(body);
      return objectMapper.convertValue(formData, targetType);
    }

    return objectMapper.readValue(body, targetType);
  }

  /**
   * Analyzes the parameters of a given method and creates a list of
   * {@link HandlerParameter} metadata records. This is a one-time operation.
   *
   * @param method The method whose parameters are to be analyzed.
   * @return A list of metadata records, one for each parameter.
   */
  private List<HandlerParameter> analyzeParameters(Method method) {
    Parameter[] parameters = method.getParameters();
    return IntStream.range(0, parameters.length)
        .mapToObj(index -> {
          Parameter parameter = parameters[index];
          if (parameter.isAnnotationPresent(RequestBody.class)) {
            return new HandlerParameter(index, ParamType.REQUEST_BODY, parameter.getType(), null);
          }
          if (parameter.isAnnotationPresent(PathParameter.class)) {
            String paramName = parameter.getAnnotation(PathParameter.class).value();
            return new HandlerParameter(index, ParamType.PATH_PARAMETER, parameter.getType(), paramName);
          }
          if (parameter.isAnnotationPresent(RequestEvent.class)) {
            return new HandlerParameter(index, ParamType.REQUEST_EVENT, parameter.getType(), null);
          }
          return new HandlerParameter(index, ParamType.UNKNOWN, parameter.getType(), null);
        }).toList();
  }

  /**
   * Generates a standard, JSON-formatted error response.
   *
   * @param statusCode The HTTP status code for the response.
   * @param message    The error message to include in the response body.
   * @return A fully constructed {@link APIGatewayV2HTTPResponse}.
   */
  private APIGatewayV2HTTPResponse createErrorResponse(int statusCode, String message) {
    try {
      String errorBody = objectMapper.writeValueAsString(Map.of("error", message));
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(statusCode)
          .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
          .withBody(errorBody)
          .build();
    } catch (JsonProcessingException e) {
      return APIGatewayV2HTTPResponse.builder()
          .withStatusCode(500)
          .withBody("{\"error\":\"Internal Server Error and failed to serialize error response.\"}")
          .build();
    }
  }

  /**
   * This method scans all public methods and returns the first one named
   * "invoke".
   * It is typically used during initialization to cache a reference to the target
   * method.
   *
   * @return The {@link Method} instance corresponding to the 'invoke' method.
   * @throws IllegalStateException if the 'invoke' method cannot be found in this
   *                               class.
   */
  private Method findInvokeMethod() {
    return Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().equals("invoke")).findFirst()
        .orElseThrow(() -> new IllegalStateException("No 'invoke' method found in handler class " + this.getClass().getName()));
  }

  /**
   * This utility method unwraps an {@link InvocationTargetException} to return
   * the underlying exception that was the true cause. For all other throwable
   * types, it returns the original throwable unchanged.
   *
   * @param throwable The throwable to inspect.
   * @return The root cause, or the original throwable if it is not an
   *         {@link InvocationTargetException}.
   */
  private Throwable getRootCause(Throwable throwable) {
    if (throwable instanceof InvocationTargetException)
      return throwable.getCause();
    return throwable;
  }

  /**
   * This method handles standard form-data strings, such as
   * "key1=value1&key2=value2". It correctly decodes keys and values using
   * UTF-8 and safely handles malformed pairs.
   *
   * @param body The URL-encoded string to parse. Can be null or empty.
   * @return An unmodifiable {@link Map} containing the decoded key-value pairs.
   *         Returns an empty map if the input body is null or empty.
   */
  private Map<String, String> parseUrlEncodedBody(String body) {
    if (body == null || body.isEmpty())
      return Collections.emptyMap();

    Pattern ampersandPattern = Pattern.compile("&");

    return ampersandPattern.splitAsStream(body)
        .filter(pair -> pair.contains("="))
        .map(pair -> pair.split("=", 2))
        .collect(Collectors.toMap(
            keyValueArr -> decode(keyValueArr[0]),
            keyValueArr -> decode(keyValueArr[1]),
            (firstValue, secondValue) -> secondValue));
  }

  /**
   * This is a helper method to centralize decoding logic and handle
   * potential {@link IllegalArgumentException} if the input string contains
   * invalid encoding.
   *
   * @param encodedValue The URL-encoded string segment (key or value).
   * @return The decoded string. If decoding fails, it returns the original,
   *         un-decoded string as a fallback.
   */
  private String decode(String encodedValue) {
    try {
      return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return encodedValue;
    }
  }

  /**
   * This method supports conversion to {@link String}, {@link Integer},
   * {@link Long}, and their corresponding primitive types.
   *
   * @param value      The string representation of the value to convert. May be
   *                   null.
   * @param targetType The {@link Class} of the desired type (e.g.,
   *                   Integer.class).
   * @return An {@link Object} of the target type, or null if the input value was
   *         null.
   * @throws NumberFormatException    if the value is not a valid representation
   *                                  of the target numeric type.
   * @throws IllegalArgumentException if the targetType is not supported.
   */
  private Object convertType(String value, Class<?> targetType) {
    if (value == null)
      return null;

    return switch (targetType) {
      case Class<?> classType when classType == String.class -> value;
      case Class<?> classType when classType == Integer.class || classType == int.class -> Integer.parseInt(value);
      case Class<?> classType when classType == Long.class || classType == long.class -> Long.parseLong(value);
      case Class<?> classType when classType == Boolean.class || classType == boolean.class -> Boolean.parseBoolean(value);
      default -> throw new IllegalArgumentException("Unsupported parameter type for conversion: " + targetType.getName());
    };
  }
}
