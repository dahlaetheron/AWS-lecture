package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BookHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final Table table = dynamoDB.getTable("Books");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        context.getLogger().log("Event: " + event + "\n");

        String rawBody = (String) event.get("body"); // body is a JSON string
        Map<String, Object> payload = new HashMap<>();

        try {
            if (rawBody != null) {
                payload = objectMapper.readValue(rawBody, Map.class);
            }
        } catch (Exception e) {
            context.getLogger().log("Error parsing body: " + e.getMessage());
        }

        String action = (String) payload.get("action");
        Map<String, Object> body = (Map<String, Object>) payload.get("body");

        String result;
        try {
            if (action == null) {
                result = "{\"error\":\"Missing action\"}";
            } else {
                switch (action.toLowerCase()) {
                    case "create":
                        result = createBook(body);
                        break;
                    case "get":
                        result = getBook((String) body.get("id"));
                        break;
                    case "update":
                        result = updateBook(body);
                        break;
                    case "delete":
                        result = deleteBook((String) body.get("id"));
                        break;
                    default:
                        result = "{\"error\":\"Unknown action: " + action + "\"}";
                }
            }
        } catch (Exception e) {
            result = "{\"error\":\"" + e.getMessage() + "\"}";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", result);

        return response;
    }

    private String createBook(Map<String, Object> body) {
        String id = UUID.randomUUID().toString();
        table.putItem(new Item()
                .withPrimaryKey("id", id)
                .withString("title", (String) body.get("title"))
                .withString("author", (String) body.get("author"))
                .withNumber("year", (Integer) body.get("year")));
        return "{\"message\":\"Book created\",\"id\":\"" + id + "\"}";
    }

    private String getBook(String id) {
        Item item = table.getItem("id", id);
        return (item == null) ? "{\"message\":\"Book not found\"}" : item.toJSON();
    }

    private String updateBook(Map<String, Object> body) {
        String id = (String) body.get("id");
        table.updateItem("id", id,
                new AttributeUpdate("title").put((String) body.get("title")),
                new AttributeUpdate("author").put((String) body.get("author")),
                new AttributeUpdate("year").put((Integer) body.get("year")));
        return "{\"message\":\"Book updated\",\"id\":\"" + id + "\"}";
    }

    private String deleteBook(String id) {
        table.deleteItem("id", id);
        return "{\"message\":\"Book deleted\",\"id\":\"" + id + "\"}";
    }
}
