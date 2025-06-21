import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "employee_db";
    private static MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DB_NAME);
    }

    public static void closeConnection() {
        mongoClient.close();
    }
}
