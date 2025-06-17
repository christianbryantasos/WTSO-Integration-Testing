package FrameworkTests;

import com.asos.ip.helper.JSONTools.JSONTools;
import com.asos.ip.helper.configLoader.AppSecretConverter;
import com.asos.ip.helper.configLoader.ConfigLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AppSecretConverterTests {

    Map<String, Object> configMap = new HashMap<>();
    AppSecretConverter appSecretConverter = new AppSecretConverter();
    ConfigLoader configLoader = new ConfigLoader();
    
    @Test
    @DisplayName("Testing Replacing App Secret in String")
    void testingReplacingAppSecretInString() throws AppSecretConverter.SecretConversionException {

        configMap.put("interfaceID", "parentYaml");
        configMap.put("testInterfaceReference", "${interfaceID}-event-s1");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals("parentYaml-event-s1", configMap.get("testInterfaceReference"));
    }

    @Test
    @DisplayName("Testing Replacing App Secret Multiple Keys and Strings")
    void testingReplacingAppSecretMultipleKeysAndStrings() throws AppSecretConverter.SecretConversionException {

        configMap.put("interfaceID", "parentYaml");
        configMap.put("testInterfaceReference", "${interfaceID}-event-s1");
        configMap.put("testKey", "testValue");
        configMap.put("testReplacement", "${testKey}-event-s2");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals("parentYaml-event-s1", configMap.get("testInterfaceReference"));
        assertEquals("testValue-event-s2", configMap.get("testReplacement"));

    }

    @Test
    @DisplayName("Testing Replacing AppSecret 2 in Same String")
    void testingReplacingAppSecret2inSameString() throws AppSecretConverter.SecretConversionException {

        configMap.put("interfaceID", "parentYaml");
        configMap.put("intr.env", "dev");
        configMap.put("testInterfaceReference", "${interfaceID}-${intr.env}-event-s1");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals("parentYaml-dev-event-s1", configMap.get("testInterfaceReference"));
    }

    @Test
    @DisplayName("Testing Replacing App Secret Yaml File")
    void testingReplacingAppSecretYamlFile() throws IOException, AppSecretConverter.SecretConversionException {


        configMap = configLoader.loadParentConfigFile("local");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        JSONObject convertedMap = new JSONObject(configMap);

        JSONTools jsonTools = new JSONTools();

        JSONObject expectedConvertedMap = jsonTools.getJSONFromResources("AppSecretConverter/convertedLocal.json");

        JSONAssert.assertEquals(expectedConvertedMap, convertedMap, true);
    }

    @Test
    @DisplayName("Testing Secret Conversion Exception")
    void testingSecretConversionException() {

        configMap.put("testInterfaceReference", "${interfaceID}-event-s1");

        Assertions.assertThrows(AppSecretConverter.SecretConversionException.class, () -> {
            appSecretConverter.convertAppSecrets(configMap);
        });
    }

    @Test
    @DisplayName("Testing Replacing Lots of App Secrets in One String")
    void testingReplacingLotsOfAppSecretsInOneString() throws AppSecretConverter.SecretConversionException {

        int testNumber = 1000000;

        for (int i = 1; i <= testNumber; i++) {
            configMap.put("test" + i, String.valueOf(i));
        }

        StringBuilder testAppSecretBuilder = new StringBuilder();
        StringBuilder testAppSecretBuilderExpected = new StringBuilder();
        for (int i = 1; i <= testNumber; i++) {
            testAppSecretBuilder.append("${test").append(i).append("}");
            testAppSecretBuilderExpected.append(i);
            if (i < testNumber) {
                testAppSecretBuilder.append("-");
                testAppSecretBuilderExpected.append("-");
            }
        }
        configMap.put("testAppSecret", testAppSecretBuilder.toString());

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals(testAppSecretBuilderExpected.toString(), configMap.get("testAppSecret"));
    }

    @Test
    @DisplayName("Testing Blank Value for App Secret")
    void testingBlankValueForAppSecret() throws AppSecretConverter.SecretConversionException {

        configMap.put("test", "");
        configMap.put("testAppSecret", "${test}");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals("", configMap.get("testAppSecret"));

    }

    @Test
    @DisplayName("Testing Integer Value For App Secret")
    void testingIntegerValueForAppSecret() throws AppSecretConverter.SecretConversionException {

        configMap.put("test", 1);
        configMap.put("testAppSecret", "${test}");

        configMap = appSecretConverter.convertAppSecrets(configMap);

        assertEquals("1", configMap.get("testAppSecret"));
    }
}
