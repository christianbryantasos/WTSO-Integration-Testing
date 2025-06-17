package com.asos.ip.helper.configLoader;

import com.asos.core.parser.yamlparser.HelmYamlParser;
import com.asos.ip.helper.Constants.FrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigLoader is responsible for loading and combining configuration files
 * from YAML sources. It handles both parent and child configurations, resolving
 * application secrets within the configurations.
 */
public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    AppSecretConverter appSecretConverter;

    /**
     * Combines two configuration maps, prioritizing child values over parent values
     * for overlapping keys.
     *
     * @param parentConfigMap The parent configuration map.
     * @param childConfigMap The child configuration map.
     * @return A new map containing combined configurations.
     */
    private Map<String, Object> returnCombinedConfigMap(Map<String, Object> parentConfigMap, Map<String, Object> childConfigMap) {
        Map<String, Object> combinedConfigMap = new HashMap<>();
        combinedConfigMap.putAll(parentConfigMap);
        combinedConfigMap.putAll(childConfigMap);
        logger.debug("Combined config map created with {} entries.", combinedConfigMap.size());
        return combinedConfigMap;
    }

    /**
     * Returns a FileInputStream for a specified YAML file based on the environment.
     *
     * @param pathToYamlFile The base path to the YAML file.
     * @param environment The environment suffix to append to the file path.
     * @return A FileInputStream for the YAML file, or null if an error occurs.
     */
    private FileInputStream returnYamlFileInputStream(String pathToYamlFile, String environment) {
        StringBuilder yamlFilePath = new StringBuilder(pathToYamlFile)
                .append(environment)
                .append(".yaml");

        try {
            File yamlFile = new File(yamlFilePath.toString());
            logger.info("Attempting to load YAML file from path: {}", yamlFile.getAbsolutePath());
            return new FileInputStream(yamlFile);
        } catch (Exception e) {
            logger.error("Failed to load YAML file from path: {} for environment: {}", yamlFilePath, environment, e);
            return null;
        }
    }

    /**
     * Loads and combines the parent and child configuration files based on the environment.
     *
     * @param interfaceSpecificYamlPath The path to the child YAML file.
     * @param environment The environment for which to load configurations.
     * @return A map containing the combined configurations, with secrets resolved.
     */
    public Map<String, Object> loadCombinedConfigFile(String interfaceSpecificYamlPath, String environment) {
        try {
            logger.info("Loading combined config file for environment: {}", environment);

            Map<String, Object> parentConfigMap = loadParentConfigFile(environment);
            Map<String, Object> childConfigMap = loadAnyConfigFile(interfaceSpecificYamlPath, environment);

            if (parentConfigMap == null || childConfigMap == null) {
                logger.warn("Parent or child config map is null, returning null combined config.");
                return null;
            }

            Map<String, Object> combinedConfigMap = returnCombinedConfigMap(parentConfigMap, childConfigMap);
            logger.info("Successfully loaded and combined config files for environment: {}", environment);
            return appSecretConverter.convertAppSecrets(combinedConfigMap);

        } catch (Exception e) {
            logger.error("Error loading combined config file for environment: {}", environment, e);
            return null;
        }
    }

    /**
     * Loads the parent configuration file for a specified environment.
     *
     * @param environment The environment for which to load the parent configuration.
     * @return A map containing the parent configuration, with secrets resolved.
     */
    public Map<String, Object> loadParentConfigFile(String environment) {
        try {
            logger.debug("Loading parent config for environment: {}", environment);

            FileInputStream parentYamlFile = returnYamlFileInputStream(FrameworkConstants.DEFAULT_PARENT_YAML_DIRECTORY, environment);

            if (parentYamlFile == null) {
                logger.warn("Parent YAML file input stream is null for environment: {}", environment);
                return null;
            }

            Map<String, Object> parentConfigMap = HelmYamlParser.loadPropertiesFromYaml(parentYamlFile);

            logger.info("Successfully loaded parent config for environment: {}", environment);
            return parentConfigMap;

        } catch (Exception e) {
            logger.error("Error loading parent config file for environment: {}", environment, e);
            return null;
        }
    }

    /**
     * Loads any configuration file from a specified path for a given environment.
     *
     * @param pathToYamlFile The path to the YAML file to load.
     * @param environment The environment for which to load the configuration.
     * @return A map containing the configuration, with secrets resolved.
     */
    public Map<String, Object> loadAnyConfigFile(String pathToYamlFile, String environment) {
        try {
            logger.debug("Loading config file from path: {} for environment: {}", pathToYamlFile, environment);

            FileInputStream yamlFileInputStream = returnYamlFileInputStream(pathToYamlFile, environment);

            if (yamlFileInputStream == null) {
                logger.warn("YAML file input stream is null for path: {} and environment: {}", pathToYamlFile, environment);
                return null;
            }

            Map<String, Object> configMap = HelmYamlParser.loadPropertiesFromYaml(yamlFileInputStream);
            logger.info("Successfully loaded config file from path: {} for environment: {}", pathToYamlFile, environment);
            return appSecretConverter.convertAppSecrets(configMap);

        } catch (Exception e) {
            logger.error("Error loading config file from path: {} for environment: {}", pathToYamlFile, environment, e);
            return null;
        }
    }

    /**
     * Converts a map of configuration properties into a Properties object.
     *
     * @param hashMap The map of configuration properties.
     * @return A Properties object populated with the entries from the map.
     */
    public static Properties convertMapToProperties(Map<String, Object> hashMap) {
        Properties properties = new Properties();
        logger.debug("Converting map to Properties object with {} entries.", hashMap.size());

        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }

        return properties;
    }
}
