package org.JarApiAutomation.dbConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnectionTest {
    public static void main(String[] args) {
        String mongoUri = "mongodb+srv://staging-qa:rfyhBgx5CLm6jEVX@cluster0-pl-0.7kwfi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        MongoClient mongoClient = null;

        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSocketSettings(builder -> builder.connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);

            // Test connection by listing databases
            MongoDatabase db = mongoClient.getDatabase("auth");
            MongoCollection<Document> collection = db.getCollection("smsDeliveryReports");
            System.out.println(collection.getNamespace());


            System.out.println("Successfully connected to MongoDB Atlas!");
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mongoClient != null) mongoClient.close();
        }
    }
}
