package com.asos.ip.steps;

import com.asos.ip.config.EndpointManager;
import com.asos.ip.helper.Constants.FrameworkConstants;
import com.asos.ip.helper.JSONTools.JSONTools;
import com.asos.ip.helper.TestData.TestDataHelper;
import com.asos.ip.helper.mongoDB.MongoDBConnectionManager;
import com.asos.ip.helper.mongoDB.MongoDBController;
import com.mongodb.client.MongoCollection;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MongoDBSteps {

    MongoDBConnectionManager mongoDBConnectionManager;
    TestDataHelper testDataHelper = new TestDataHelper();

    @Autowired
    public MongoDBSteps(EndpointManager endpointManager) {
        this.mongoDBConnectionManager = endpointManager.getMongoDBConnectionManager();
    }

    @Given("I upload a document with ID {string} to the MongoDB server {string} to the MongoDB {string} database to the {string} collection")
    public void uploadFileToMongoDb(String testDocumentID, String serverName, String databaseName, String databaseCollection) throws IOException {

        MongoDBController mongoDBController = returnMongoDBController(serverName, databaseName, databaseCollection);
        mongoDBController.addDocumentToCosmosDB(testDocumentID, JSONTools.getJSONFromResources(testDocumentID));
    }

    @Given("the MongoDB {string} server in the {string} database in the {string} collection does not contain a document with ID {string}")
    public void deleteFileFromMongoDb(String serverName, String databaseName, String databaseCollection, String testDocumentID) {

        MongoDBController mongoDBController = returnMongoDBController(serverName, databaseName, databaseCollection);
        mongoDBController.deleteFile(testDocumentID);
    }

    @Then("the document with ID {string} is present in the MongoDB {string} server in the {string} database in the {string} collection")
    public void verifyDocumentExists(String testDocumentID, String serverName, String databaseName, String databaseCollection) throws InterruptedException {

        MongoDBController mongoDBController = returnMongoDBController(serverName, databaseName, databaseCollection);
        boolean exists = mongoDBController.waitForDocumentToExist(testDocumentID);

        assertTrue(exists, "Expected document to exist in MongoDB, but it was not found.");
    }

    @And("I upload the file {string} to the MongoDB {string} server in the {string} database in the {string} collection")
    public void uploadFileToMongoDbCollection(String fileName, String serverName, String databaseName, String databaseCollection) throws IOException {

        String filePath = testDataHelper.returnFilePathFromInputResourcesDirectory(fileName);
        JSONObject jsonToUpload = (JSONTools.getJSONFromResources(filePath));
        String id = JSONTools.getAttributeFromJSONObject("_id", jsonToUpload);

        MongoDBController mongoDBController = returnMongoDBController(serverName, databaseName, databaseCollection);
        mongoDBController.addDocumentToCosmosDB(id, jsonToUpload);
    }

    @Then("Document with ID {string} in the MongoDB {string} server in the {string} database in the {string} collection should match the expected output in the file {string} ignoring fields {string}")
    public void verifyJsonDataFromMongo(String documentID, String serverName, String databaseName, String databaseCollection, String expectedOutputFileName, String ignoredFields) throws IOException {

        String expectedOutputFilePath = FrameworkConstants.DEFAULT_RESOURCES_OUTPUT_DIRECTORY + expectedOutputFileName;

        if (documentID == null || documentID.trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID must not be null or empty");
        }

        MongoDBController mongoDBController = returnMongoDBController(serverName, databaseName, databaseCollection);
        JSONObject actualJson = mongoDBController.fetchJsonFromMongo(documentID);
        String[] fieldsToIgnore = testDataHelper.parseIgnoredFieldsCamelCase(ignoredFields);

        JSONTools.assertJsonEquality(actualJson, expectedOutputFilePath, fieldsToIgnore);
    }

    private MongoDBController returnMongoDBController(String serverName, String databaseName, String databaseCollection) {
        mongoDBConnectionManager.setActiveMongoConnection(serverName);
        MongoCollection<Document> mongoDBCollection = mongoDBConnectionManager.getActiveMongoCollection(databaseName, databaseCollection);
        return new MongoDBController(mongoDBCollection);
    }

}