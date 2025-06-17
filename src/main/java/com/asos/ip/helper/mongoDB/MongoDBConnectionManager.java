package com.asos.ip.helper.mongoDB;

import com.asos.ip.config.LoadConfigurations;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class MongoDBConnectionManager {

    private final Properties properties;
    Map<String, String> mongoProperties = new HashMap<>();

    MongoDBConnection activeMongoDBConnection;
    Map<String, MongoDBConnection> mongoDBConnectionMap = new HashMap<>();

    public MongoDBConnectionManager(LoadConfigurations loadConfigurations) {

        this.properties = loadConfigurations.getConfigProperties();
        gatherMongoProperties();
    }

    public void setActiveMongoConnection(String server) {

        if(mongoDBConnectionMap.isEmpty()) {
            preloadMongoDBConnections();
        }

        String serverName = server.toLowerCase();
        activeMongoDBConnection = mongoDBConnectionMap.get(serverName);
    }

    public MongoCollection<Document> getActiveMongoCollection(String database, String collection) {

        String databaseName = database.toLowerCase();
        String collectionName = collection.toLowerCase();

        activeMongoDBConnection.setActiveMongoCollection(databaseName, collectionName);

        return activeMongoDBConnection.getActiveMongoCollection();
    }

    private void gatherMongoProperties() {
        for (String propertyKey : properties.stringPropertyNames()) {
            if (propertyKey.startsWith("azure.docdb.")) {
                mongoProperties.put(propertyKey, properties.getProperty(propertyKey));
            }
        }
    }

    private void preloadMongoDBConnections() {
        // Mongo Connection String Yaml Keys Must Follow The Following Format: "azure.docdb.<server_name>.included.uri"

        String serverKeyBeginning = "azure.docdb.";
        String serverKeyEnding = ".included.uri";

        for (String propertyKey : mongoProperties.keySet()) {

            if (propertyKey.endsWith(serverKeyEnding)) {

                String serverKeyNameSubstring = propertyKey.substring(serverKeyBeginning.length(), propertyKey.length() - serverKeyEnding.length());
                mongoDBConnectionMap.put(serverKeyNameSubstring, new MongoDBConnection(properties.getProperty(propertyKey)));
                preloadMongoDBDatabasesForServer(serverKeyNameSubstring);
            }
        }
    }

    private void preloadMongoDBDatabasesForServer(String serverKeyNameSubstring) {
        // Mongo Database Yaml Keys Must Follow The Following Format: "azure.docdb.<server_name>.<database_name>.database.name"

        String databaseKeyBeginning = "azure.docdb." + serverKeyNameSubstring + ".";
        String databaseKeyEnding = ".database.name";

        for (String propertyKey : mongoProperties.keySet()) {
            if (propertyKey.startsWith(databaseKeyBeginning) && propertyKey.endsWith(databaseKeyEnding)) {

                String databaseKeyNameSubstring = propertyKey.substring(databaseKeyBeginning.length(), propertyKey.length() - databaseKeyEnding.length());
                String databaseNameValue = properties.getProperty(propertyKey);

                mongoDBConnectionMap.get(serverKeyNameSubstring).addMongoDatabase(databaseKeyNameSubstring);

                preloadMongoDBCollectionsForDatabase(serverKeyNameSubstring, databaseKeyNameSubstring, databaseNameValue);
            }
        }
    }

    private void preloadMongoDBCollectionsForDatabase(String serverKeyNameSubstring, String databaseKeyNameSubstring, String databaseNameValue) {
        // Mongo Collection Yaml Keys Must Follow The Following Format: "azure.docdb.<server_name>.<database_name>.<collection_name>.collection.name"

        String collectionKeyBeginning = "azure.docdb." + serverKeyNameSubstring + "." + databaseKeyNameSubstring + ".";
        String collectionKeyEnding = ".collection.name";

        for (String propertyKey : mongoProperties.keySet()) {
            if (propertyKey.startsWith(collectionKeyBeginning) && propertyKey.endsWith(collectionKeyEnding)) {

                String collectionKeyNameSubstring = propertyKey.substring(collectionKeyBeginning.length(), propertyKey.length() - collectionKeyEnding.length());
                String collectionNameValue = properties.getProperty(propertyKey);

                mongoDBConnectionMap.get(serverKeyNameSubstring).addMongoCollection(databaseKeyNameSubstring, databaseNameValue, collectionKeyNameSubstring, collectionNameValue);
            }
        }
    }
}
