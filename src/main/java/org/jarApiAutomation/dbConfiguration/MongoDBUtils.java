package org.jarApiAutomation.dbConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MongoDBUtils {

    // Volatile to ensure thread-safety
    private static volatile MongoClient mongoClient;
    public static Document document;

    // Private constructor to prevent instantiation
    private MongoDBUtils() {}

    /**
     * Initialize MongoClient singleton (lazy, thread-safe)
     */
    public static void initializeMongoClient() {
        try {
            if (mongoClient == null) {
                synchronized (MongoDBUtils.class) {
                    if (mongoClient == null) {
                        mongoClient = MongoClients.create(
                                MongoClientSettings.builder()
                                          .applyConnectionString(new ConnectionString(MongoDBConstants.MONGO_DB_URL))
                                        .applyToSocketSettings(builder -> builder.connectTimeout(60, TimeUnit.SECONDS))
                                        .build()
                        );
                        log.info("MongoDB Client Initialized");
                    }
                }
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("Mongo Initializing Failed: {0}", e.getStackTrace()));
        }
    }

    /**
     * Get MongoClient instance (singleton)
     * to get all databases, monitor connections, or run admin commands. Otherwise there is no usage of this method
     */
    public static MongoClient getClient() {
        initializeMongoClient();
        return mongoClient;
    }

    /**
     * Returns MongoDatabase instance from the singleton client
     */
    public static MongoDatabase getDatabase(String dbName) {
        return getClient().getDatabase(dbName);
    }

    /**
     * Returns MongoCollection instance from the singleton client
     */
    public static MongoCollection<Document> getCollection(String dbName, String collectionName) {
        return getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * Fetch a single document based on filter and sort
     * @param dbName Database name
     * @param collection Collection name
     * @param filterKey Field to filter on
     * @param value Value to match
     * @param sortField Field to sort by (descending)
     */
    public static Document fetchData(String dbName, String collection, String filterKey, String value, String sortField) {
        try {
            MongoCollection<Document> dbCollection = getCollection(dbName, collection);
            System.out.println(value);
            Document query = new Document(filterKey, value);
            Document sort = new Document(sortField, -1);

            Document document = dbCollection.find(query)
                                            .sort(sort)
                                            .limit(1)
                                            .first();  // returns first document or null if none found
            if (document != null) {
                System.out.println("Fetched Document: " + document.toJson());
            } else {
                System.out.println("No Document found for Query: " + query.toJson());
            }
            return document;
        } catch (Exception e) {
            log.error(MessageFormat.format("Error while fetching data: {0}", e.getStackTrace()));
            return null;
        }
    }

    /**
     * Fetch from Main DB
     */
    public static Document fetchDataFromMainDB(String collection, String filterKey, String value, String sortField) {
        return fetchData(MongoDBConstants.DB_NAME, collection, filterKey, value, sortField);
    }

    /**
     * Close the MongoClient
     */
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            System.out.println("[MongoDB] Connection closed ");
        }
    }
}
