package org.jarApiAutomation.dbConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MongoDBUtils {

    // Volatile to ensure thread-safety
    private volatile MongoClient mongoClient;
    private final String uri;

    public MongoDBUtils(String uri) {
        this.uri = uri;
    }

    /**
     * Thread-safe, lazy Singleton
     */
    public MongoClient initializeClient() {
        if (mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    mongoClient = MongoClients.create(
                            MongoClientSettings.builder()
                                          .applyConnectionString(new ConnectionString(uri))
                                        .applyToSocketSettings(builder -> builder.connectTimeout(60, TimeUnit.SECONDS))
                                    .build()
                    );
                        log.info("MongoDB Client Initialized");
                }
            }
        }
        return mongoClient;
    }


    /**
     * Returns MongoDatabase instance from the singleton client
     */
    public MongoDatabase getDatabase(String dbName) {
        return initializeClient().getDatabase(dbName);
    }

    /**
     * Returns MongoCollection instance from the singleton client
     */
    public MongoCollection<Document> getCollection(String dbName, String collectionName) {
        return getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * Fetch a single document based on filter and sort
     *
     * @param dbName     Database name
     * @param collection Collection name
     * @param filterKey  Field to filter on
     * @param value      Value to match
     * @param sortField  Field to sort by (descending)
     */
    public Document fetchData(String dbName, String collection, String filterKey, String value, String sortField) {
        try {
            MongoCollection<Document> dbCollection = getCollection(dbName, collection);
            Document query;
            if ("_id".equals(filterKey)) {
                query = new Document("_id", new ObjectId(value));
            } else {
                query = new Document(filterKey, value);
            }
            Document sort = new Document(sortField, -1);

            Document document = dbCollection.find(query)
                    .sort(sort)
                    .limit(1)
                    .first();  // returns first document or null if none found
            if (document != null) {
                log.info("Fetched Document: {}", document);
            } else {
                log.info("No Document found for Query: {}", query);
            }
            return document;
        } catch (Exception e) {
            log.info("Error while fetching data: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the MongoClient
     */
    public void closeConnection() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
            log.info("MongoDB connection closed.");}
        } catch (Exception e) {
            log.error("Error closing MongoDB connection: {}", e.getMessage());
        }

    }
}
