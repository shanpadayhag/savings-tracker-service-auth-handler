package com.savingstracker.auth_handler.functions;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

@Component("login")
public class LoginFunction implements Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  @Override
  public APIGatewayV2HTTPResponse apply(APIGatewayV2HTTPEvent event) {
    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(200)
        .withBody(event.toString())
        .build();
  }
}
