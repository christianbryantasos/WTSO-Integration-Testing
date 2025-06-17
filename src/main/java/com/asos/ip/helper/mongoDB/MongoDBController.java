package com.asos.ip.helper.mongoDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;


public class MongoDBController {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBController.class);

    MongoCollection<Document> mongoCollection;

    public MongoDBController(MongoCollection<Document> mongoDBCollection) {
        this.mongoCollection = mongoDBCollection;
    }

    public void addDocumentToCosmosDB(String documentId, JSONObject document) {
        try {
            Document doc = Document.parse(document.toString());
            doc.put("_id", documentId);
            mongoCollection.insertOne(doc);
            logger.info("Document with ID '{}' added to collection '{}'", documentId, mongoCollection.getNamespace().getCollectionName());
        } catch (Exception e) {
            logger.error("Error adding document with ID '{}' to collection '{}': {}", documentId, mongoCollection.getNamespace().getCollectionName(), e.getMessage(), e);
        }
    }

    public Document readDocument(String collectionName, String documentId) {
        try {
            Document doc = mongoCollection.find(eq("_id", documentId)).first();
            if (doc != null) {
                logger.info("Document with ID '{}' retrieved from collection '{}'", documentId, collectionName);
            } else {
                logger.warn("Document with ID '{}' not found in collection '{}'", documentId, collectionName);
            }
            return doc;
        } catch (Exception e) {
            logger.error("Error reading document with ID '{}' from collection '{}': {}", documentId, collectionName, e.getMessage(), e);
            return null;
        }
    }

    public JSONObject getDocumentFromCosmosDB(String documentId) {
        try {
            Document doc = readDocument(mongoCollection.getNamespace().getCollectionName(), documentId);
            if (doc != null) {
                logger.info("Document with ID '{}' converted to JSON", documentId);
                return new JSONObject(doc);
            } else {
                logger.warn("Document with ID '{}' not found for conversion to JSON", documentId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error converting document with ID '{}' to JSON: {}", documentId, e.getMessage(), e);
            return null;
        }
    }

    public boolean doesDocumentExist(String documentId) {
        try {
            logger.info("Checking if Document with ID '{}' exists in collection '{}'", documentId, mongoCollection.getNamespace().getCollectionName());
            boolean exists = mongoCollection.find(eq("_id", documentId)).first() != null;
            if (exists) {
                logger.info("Document with ID '{}' exists in collection '{}'", documentId, mongoCollection.getNamespace().getCollectionName());
            } else {
                logger.warn("Document with ID '{}' does not exist in collection '{}'", documentId, mongoCollection.getNamespace().getCollectionName());
            }
            return exists;
        } catch (Exception e) {
            logger.error("Error checking existence of document with ID '{}' in collection '{}': {}", documentId, mongoCollection.getNamespace().getCollectionName(), e.getMessage(), e);
            return false;
        }
    }

    public void deleteFile(String documentId) {
        try {
            mongoCollection.deleteOne(eq("_id", documentId));
            logger.info("Document with ID '{}' deleted from collection '{}'", documentId, mongoCollection.getNamespace().getCollectionName());
        } catch (Exception e) {
            logger.error("Error deleting document with ID '{}' from collection '{}': {}", documentId, mongoCollection.getNamespace().getCollectionName(), e.getMessage(), e);
        }
    }

    public void deleteMultipleDocumentsBasedOffQueryResults(Bson query) {
        try {
            DeleteResult result = mongoCollection.deleteMany(query);
            logger.info("Deleted {} documents from collection: {}", result.getDeletedCount(), mongoCollection.getNamespace().getCollectionName());
        } catch (Exception e) {
            logger.info("Error occurred while deleting documents: {}", e.getMessage());
        }
    }

    public boolean waitForDocumentToExist(String documentId) throws InterruptedException {
        for (int timeoutCounter = 0; !doesDocumentExist(documentId); timeoutCounter++) {
            if (timeoutCounter > 12) throw new RuntimeException("Document not found in CosmosDB. Test timed out.");
            Thread.sleep(10000);
        }
        return true;
        //If the timeout occurs, it will throw an exception, and the code won't reach the return statement
    }

    public JSONObject fetchJsonFromMongo(String documentId) {
        logger.info("Fetching JSON data from MongoDB for documentId: {}", documentId);
        // Fetching document from MongoDB/CosmosDB
        JSONObject fetchedJson = getDocumentFromCosmosDB(documentId);

        // Log the retrieved JSON for debugging purposes
        if (fetchedJson != null) {
            logger.debug("Fetched JSON data: {}", fetchedJson.toString(2)); // Pretty-print for readability
        } else {
            logger.warn("No data found for documentId: {}", documentId);
        }

        return fetchedJson;
    }

}