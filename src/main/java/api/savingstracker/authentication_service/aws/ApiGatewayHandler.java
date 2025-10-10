package api.savingstracker.authentication_service.aws;

import api.savingstracker.authentication_service.annotations.PathParameter;
import api.savingstracker.authentication_service.annotations.RequestBody;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public abstract class ApiGatewayHandler implements Function<APIGatewayProxyRequestEvent, Object> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Object apply(APIGatewayProxyRequestEvent event) {
    Method invokeMethod = findInvokeMethod();

    try {
      Parameter[] parameters = invokeMethod.getParameters();
      Object[] arguments = new Object[parameters.length];

      for (int i = 0; i < parameters.length; i++) {
        Parameter param = parameters[i];
        if (param.isAnnotationPresent(RequestBody.class)) {
          arguments[i] = objectMapper.readValue(event.getBody(), param.getType());
        } else if (param.isAnnotationPresent(PathParameter.class)) {
          String paramName = param.getAnnotation(PathParameter.class).value();
          String paramValue = event.getPathParameters().get(paramName);
          arguments[i] = convertType(paramValue, param.getType());
        }
      }

      return invokeMethod.invoke(this, arguments);
    } catch (Exception exception) {
      return createResponse(500, "Internal Server Error.");
    }
  }

  protected APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
    try {
      String responseBody = objectMapper.writeValueAsString(body);
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(statusCode)
          .withHeaders(Map.of("Content-Type", "application/json"))
          .withBody(responseBody);
    } catch (Exception e) {
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(500)
          .withBody("{\"error\": \"Failed to serialize response body.\"}");
    }
  }

  private Method findInvokeMethod() {
    return Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().equals("invoke"))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No 'invoke' method found in handler class " + this.getClass().getName()));
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
}
