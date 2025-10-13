package api.savingstracker.authentication_service.aws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ApiResponse {
  @JsonProperty("statusCode")
  private int statusCode;

  @JsonProperty("headers")
  private Map<String, Object> headers;

  @JsonProperty("cookies")
  private List<String> cookies;

  @JsonProperty("body")
  private String body;
}
