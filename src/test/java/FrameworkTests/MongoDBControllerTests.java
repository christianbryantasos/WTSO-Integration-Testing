package FrameworkTests;

import com.asos.ip.fluent.TestSettings;
import com.asos.ip.helper.JSONTools.JSONTools;
import com.asos.ip.helper.mongoDB.MongoDBController;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

public class MongoDBControllerTests {

    private Properties configProperties = new Properties();
    private TestSettings testSettings;
    private MongoDBController mongoDBController;


    @Test
    @DisplayName("testing Mongodb verifier works")
    void testingMongodbVerifierWorks() throws IOException {

        mongoDBController.addDocumentToCosmosDB("114303437_1", new JSONObject(JSONTools.getJSONFromResources("Item/Output/bam020mPublishedWACFile.json")));

        boolean documentExists = mongoDBController.doesDocumentExist("114303437_1");

        Assertions.assertTrue(documentExists);
    }

  //  @AfterEach
   // public void tearDown() {
    //    mongoDBController.deleteFileFromCosmosDb("114303437_1");
   // }
}
