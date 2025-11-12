package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.Map;

public class HelloHandler {
  public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event, Context context) {
    String name = "World";
    Map<String,String> qs = event.getQueryStringParameters();
    if (qs != null && qs.get("name") != null) {
      name = qs.get("name");
    }
    String body = "{\"message\":\"Hello, " + name + "!\"}";
    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(200)
        .withHeaders(Map.of("Content-Type","application/json"))
        .withBody(body)
        .build();
  }
}




