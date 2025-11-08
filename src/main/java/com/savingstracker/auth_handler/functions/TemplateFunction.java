package com.savingstracker.auth_handler.functions;

import org.springframework.stereotype.Component;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.savingstracker.auth_handler.aws.APIGatewayHTTPResponse;
import com.savingstracker.auth_handler.aws.APIGatewayHandler;

// @Component("template")
public class TemplateFunction extends APIGatewayHandler {
  public APIGatewayV2HTTPResponse invoke() {
    return APIGatewayHTTPResponse.ok()
        .build();
  }
}
