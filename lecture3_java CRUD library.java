import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

import java.util.Map;

public class BookHandler implements RequestHandler<Map<String, Object>, String> {

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final Table table = dynamoDB.getTable("Books");

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        context.getLogger().log("EVENT: " + event);

        String httpMethod = (String) event.get("httpMethod");

        try {
            if ("POST".equalsIgnoreCase(httpMethod)) {
                // Body comes as a JSON string
                String body = (String) event.get("body");
                Map<String, String> bodyMap = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(body, Map.class);

                String id = bodyMap.get("id");
                String title = bodyMap.get("title");

                table.putItem(new Item().withPrimaryKey("BookId", id).withString("title", title));
                return "Book created: " + title;
            }

            if ("GET".equalsIgnoreCase(httpMethod)) {
                Map<String, String> pathParams = (Map<String, String>) event.get("pathParameters");
                String id = pathParams.get("id");

                Item item = table.getItem(new GetItemSpec().withPrimaryKey("BookId", id));
                return item == null ? "Book not found" : item.toJSON();
            }

            return "Unsupported method: " + httpMethod;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
