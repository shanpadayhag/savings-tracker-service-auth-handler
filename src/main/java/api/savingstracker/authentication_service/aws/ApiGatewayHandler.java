package api.savingstracker.authentication_service.aws;

import api.savingstracker.authentication_service.annotations.PathParameter;
import api.savingstracker.authentication_service.annotations.RequestBody;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    } catch (Exception e) {
      // ---- START OF NEW DEBUGGING CODE ----
      System.err.println(">>>>>>>>> DETAILED EXCEPTION REPORT <<<<<<<<<");

      Throwable cause = e;
      // Unwrap the InvocationTargetException if it exists, to get to the real error
      if (e instanceof java.lang.reflect.InvocationTargetException) {
        cause = e.getCause();
      }

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      cause.printStackTrace(pw);

      String stackTrace = sw.toString(); // stack trace as a string

      System.err.println("Root Cause Type: " + cause.getClass().getName());
      System.err.println("Root Cause Message: " + cause.getMessage());
      System.err.println("Full Stack Trace:");
      System.err.println(stackTrace);
      System.err.println(">>>>>>>>> END OF REPORT <<<<<<<<<");
      // ---- END OF NEW DEBUGGING CODE ----

      String errorBody = String.format("{\"error\": \"Internal Server Error\", \"message\": \"%s\"}",
          cause.getMessage());

      return new APIGatewayProxyResponseEvent()
          .withStatusCode(500)
          .withHeaders(Map.of("Content-Type", "application/json"))
          .withBody(errorBody);
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
