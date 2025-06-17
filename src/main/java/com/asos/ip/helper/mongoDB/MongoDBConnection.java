package com.asos.ip.helper.mongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MongoDBConnection {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

    private final MongoClient mongoClient;
    Map<String, Map<String, MongoCollection<Document>>> mongoCollectionMap = new HashMap<>();
    private MongoCollection<Document> activeMongoCollection;

    public MongoDBConnection(String connectionString) {
        this.mongoClient = setMongoClient(connectionString);
        logger.info("MongoDB connection established to server via connection string");
    }

    private MongoClient setMongoClient(String connectionString) {
        return MongoClients.create(connectionString);
    }

    public void addMongoDatabase(String databaseKeyNameSubstring) {
        mongoCollectionMap.put(databaseKeyNameSubstring, new HashMap<>());
    }

    public void addMongoCollection(String databaseKeyNameSubstring, String databaseNameValue, String collectionKeyNameSubstring, String collectionNameValue) {
        mongoCollectionMap.get(databaseKeyNameSubstring).put(collectionKeyNameSubstring, mongoClient.getDatabase(databaseNameValue).getCollection(collectionNameValue));
        logger.info("Mongo Collection: {} loaded to MongoDB Database map: {}", collectionKeyNameSubstring, databaseKeyNameSubstring);
    }

    public void setActiveMongoCollection(String databaseKeyNameSubstring, String collectionKeyNameSubstring) {
        activeMongoCollection = mongoCollectionMap.get(databaseKeyNameSubstring).get(collectionKeyNameSubstring);
    }

    public MongoCollection<Document> getActiveMongoCollection() {
        return activeMongoCollection;
    }

}


