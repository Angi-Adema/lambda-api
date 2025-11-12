package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class TodosHandler {
  private static final ObjectMapper MAPPER;
  static {
    MAPPER = new ObjectMapper();
    // Be lenient so CLI / tooling quirks don't kill us
    MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
  }

  private static final String TABLE_NAME = System.getenv().getOrDefault("TODOS_TABLE", "Todos");
  private final DynamoDbClient ddb = DynamoDbClient.create();

  public APIGatewayV2HTTPResponse handle(APIGatewayV2HTTPEvent event, Context ctx) {
    try {
      String raw = event.getBody();
      if (raw == null) {
        return response(400, "{\"error\":\"Body required\"}");
      }

      // Decode if API Gateway sent base64
      if (Boolean.TRUE.equals(event.getIsBase64Encoded())) {
        byte[] decoded = Base64.getDecoder().decode(raw);
        raw = new String(decoded, StandardCharsets.UTF_8);
      }

      String title;

      try {
        // First, try to treat body as JSON
        JsonNode node = MAPPER.readTree(raw);
        JsonNode titleNode = node.get("title");
        if (titleNode != null && !titleNode.isNull()) {
          title = titleNode.asText().trim();
        } else {
          // JSON but no title field
          return response(400, "{\"error\":\"'title' is required\"}");
        }
      } catch (Exception jsonEx) {
        // If it isn't valid JSON, treat the whole body as the title
        title = raw.trim();
      }

      if (title == null || title.isEmpty()) {
        return response(400, "{\"error\":\"'title' is required\"}");
      }

      String id = UUID.randomUUID().toString();
      Map<String, AttributeValue> item = Map.of(
          "id", AttributeValue.builder().s(id).build(),
          "title", AttributeValue.builder().s(title).build()
      );

      ddb.putItem(PutItemRequest.builder()
          .tableName(TABLE_NAME)
          .item(item)
          .build());

      // Build a clean JSON response
      String resp = MAPPER.createObjectNode()
          .put("id", id)
          .put("title", title)
          .toString();

      return response(201, resp);

    } catch (Exception e) {
      String msg = (e.getClass().getSimpleName() + ": " + e.getMessage()).replace("\"", "\\\"");
      return response(500, "{\"error\":\"" + msg + "\"}");
    }
  }

  private static APIGatewayV2HTTPResponse response(int code, String json) {
    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(code)
        .withHeaders(Map.of("Content-Type", "application/json"))
        .withBody(json)
        .build();
  }
}




