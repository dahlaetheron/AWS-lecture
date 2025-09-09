import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import java.util.Map;

public class BookHandler implements RequestHandler<Map<String, String>, String> {

    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final Table table = dynamoDB.getTable("Books");

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        String action = event.get("action");
        String id = event.get("id");
        String title = event.get("title");

        if ("create".equals(action)) {
            table.putItem(new Item().withPrimaryKey("BookId", id).withString("title", title));
            return "Book created: " + title;
        } else if ("get".equals(action)) {
            Item item = table.getItem(new GetItemSpec().withPrimaryKey("BookId", id));
            return item == null ? "Book not found" : item.toJSON();
        }
        return "Unsupported action";
    }
}
