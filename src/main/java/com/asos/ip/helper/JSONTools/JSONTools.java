package com.asos.ip.helper.JSONTools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JSONTools {
    private static final Logger logger = LoggerFactory.getLogger(JSONTools.class);

    /**
     * Creates a custom JSON comparator that ignores specified fields during JSON comparisons.
     *
     * @param ignoreFields The fields to ignore in the comparison.
     * @return A JSONComparator that ignores the specified fields.
     */
    public static JSONComparator customCompareJSON(String... ignoreFields) {
        Customization[] customizations = new Customization[ignoreFields != null ? ignoreFields.length : 0];

        if (ignoreFields != null && ignoreFields.length > 0) {
            for (int i = 0; i < ignoreFields.length; i++) {
                customizations[i] = new Customization(ignoreFields[i], (o1, o2) -> true);
            }
        }
        // Create a custom comparator with a lenient comparison mode
        JSONComparator customComparator = new CustomComparator(JSONCompareMode.LENIENT, customizations);
        return customComparator;
    }

    /**
     * Loads a JSON object from the specified resource path.
     *
     * @param resourcePath The path to the JSON resource.
     * @return A JSONObject loaded from the resource.
     * @throws IOException If an error occurs while loading the JSON.
     */
    public static JSONObject getJSONFromResources(String resourcePath) throws IOException {
        InputStream inputStream = JSONTools.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            // Attempt to load resource using file path
            File file = new File(resourcePath);
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
        }
        String jsonText = IOUtils.toString(inputStream, String.valueOf(StandardCharsets.UTF_8));
        return new JSONObject(jsonText);
    }

    /**
     * Asserts that two JSON objects are equal, with the option to ignore specified fields.
     *
     * @param actualJson The actual JSON object to compare.
     * @param expectedFilePath The path to the expected JSON file.
     * @param ignoredFields The fields to ignore in the comparison.
     * @throws IOException If an error occurs while loading the expected JSON.
     */
    public static void assertJsonEquality(JSONObject actualJson, String expectedFilePath, String... ignoredFields) throws IOException {
        logger.info("Starting JSON equality check against expectedFilePath: {}", expectedFilePath);

        // Load expected JSON from resources
        JSONObject expectedOutputFile = getJSONFromResources(expectedFilePath);
        logger.debug("Expected JSON content: {}", expectedOutputFile.toString(2));  // Pretty-print for easier reading

        try {
            // Perform the JSON assertion
            JSONAssert.assertEquals(expectedOutputFile.toString(), actualJson.toString(), customCompareJSON(ignoredFields));

            // Log success
            logger.info("JSON equality check passed for expectedFilePath: {}", expectedFilePath);
        } catch (AssertionError e) {
            // Log failure
            logger.error("JSON equality check failed for expectedFilePath: {}", expectedFilePath);
            logger.error("Expected JSON: {}", expectedOutputFile.toString(2));
            logger.error("Actual JSON: {}", actualJson.toString(2));
            throw e;  // Re-throw the exception to ensure the test fails
        }
    }

    public static String getAttributeFromJSONObject(String attribute, JSONObject jsonObject) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = jsonObject.toString();
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        logger.info("Extracting attribute " + attribute + "from Json");
        return jsonNode.get(attribute).asText();
    }

}