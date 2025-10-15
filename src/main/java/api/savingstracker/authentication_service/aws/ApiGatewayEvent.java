package api.savingstracker.authentication_service.aws;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiGatewayEvent {
  private Map<String, String> headers;
  private Map<String, String> pathParameters;
  private Map<String, String> queryStringParameters;
  private List<String> cookies;
  private String body;

  public Map<String, String> getCookiesAsMap() {
    if (this.cookies == null || this.cookies.isEmpty()) {
      return Collections.emptyMap();
    }

    return this.cookies.stream()
        .map(cookie -> cookie.split("=", 2))
        .filter(parts -> parts.length == 2)
        .collect(Collectors.toMap(
            parts -> parts[0].trim(),
            parts -> parts[1].trim()));
  }
}
