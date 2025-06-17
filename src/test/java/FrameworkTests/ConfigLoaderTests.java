package FrameworkTests;

import com.asos.ip.helper.configLoader.ConfigLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.mockito.Mockito.spy;


public class ConfigLoaderTests {
    ConfigLoader configLoader = new ConfigLoader();
    Path tempFileParent;
    Path tempFileChild;
    Map<String, Object> testConfigMap;
    String parentRoot = "../environments";
    String childRoot = "src/test/resources";


    /**
     * Creates temporary YAML files for testing.
     *
     * @throws IOException If an error occurs while creating the files.
     */
    @BeforeEach
    void createTempYamlFiles() throws IOException {
        // Delete any Temp Files that may have been created in the previous test run
        deleteTempFiles(parentRoot, childRoot);

        tempFileParent = generateTempYamlFile(parentRoot,"testParent: testParentYaml");
        tempFileChild = generateTempYamlFile(childRoot,"testChild: testChildYaml");

    }

    /**
     * Deletes the temporary YAML files created for testing.
     *
     * @param roots The root directories for the temporary files.
     * @throws IOException If an error occurs while deleting the files.
     */
    private static void deleteTempFiles(String... roots) throws IOException {
        for (String root : roots) {
            StringBuilder tempFilePathBuilder = new StringBuilder();
            tempFilePathBuilder.append(root).append("/mock.yaml");
            Files.deleteIfExists(Path.of(tempFilePathBuilder.toString()));
        }
    }

    private String getFileNameWithoutExtension(Path tempFileParent) {
        return tempFileParent.getFileName().toString().replaceFirst("[.][^.]+$", "");
    }

    /**
     * Generates a temporary YAML file with the specified contents.
     *
     * @param root The root directory for the temporary file.
     * @param contents The contents of the YAML file. - Enter Key/Value pairs in the format "key: value"
     * @return The path to the temporary YAML file.
     * @throws IOException If an error occurs while writing the file.
     */
    private Path generateTempYamlFile(String root, String contents) throws IOException {
        Path tempFile = Paths.get(root, "mock.yaml");

        StringBuilder yamlBuilder = new StringBuilder();
        yamlBuilder.append("configuration:\n")
                .append("  env:\n")
                .append("   " + contents).append("\n");

        String tempContent = yamlBuilder.toString();

        Files.writeString(tempFile, tempContent);
        return tempFile;
    }


    /**
     * Deletes the temporary YAML files created for testing.
     *
     * @throws IOException If an error occurs while deleting the files.
     */
    @AfterEach
    void deleteTempYamlFiles() throws IOException, InterruptedException {
        FileInputStream tempInputStreamParent = spy(new FileInputStream(tempFileParent.toFile()));
        FileInputStream tempInputStreamChild = spy(new FileInputStream(tempFileChild.toFile()));

        tempInputStreamParent.close();
        tempInputStreamChild.close();

        deleteTempFiles(parentRoot, childRoot);
    }


    @Test
    @DisplayName("Testing Loading Combined Config File using Temp Files")
    void testingLoadingCombinedConfigFileUsingTempFiles() {

        testConfigMap = configLoader.loadCombinedConfigFile("src/test/resources/", "mock");

        assert(testConfigMap.get("testChild").equals("testChildYaml"));
    }
}
