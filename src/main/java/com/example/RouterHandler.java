package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class RouterHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final HelloHandler hello = new HelloHandler();
  private final TodosHandler todos = new TodosHandler();

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context ctx) {
    String method = event.getRequestContext() != null && event.getRequestContext().getHttp() != null
        ? event.getRequestContext().getHttp().getMethod() : "";
    String path = event.getRawPath() != null ? event.getRawPath() : "";

    if ("GET".equalsIgnoreCase(method) && "/hello".equals(path)) {
      return hello.handle(event, ctx);
    } else if ("POST".equalsIgnoreCase(method) && "/todos".equals(path)) {
      return todos.handle(event, ctx);
    }

    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(404)
        .withHeaders(java.util.Map.of("Content-Type","application/json"))
        .withBody("{\"error\":\"Not found\"}")
        .build();
  }
}


