package com.asos.ip.helper.configLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppSecretConverter is responsible for resolving application secrets
 * represented as placeholders in the configuration map. It replaces
 * placeholders with their corresponding values from the config map.
 */
public class AppSecretConverter {

    private static final Logger logger = LoggerFactory.getLogger(AppSecretConverter.class);
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    /**
     * Converts application secrets in the provided config map.
     *
     * This method iterates over each entry in the config map, resolving
     * any placeholders found in the values. If a placeholder is resolved,
     * it replaces the original value in a new map.
     *
     * @param configMap The original configuration map containing secrets.
     * @return A new map with resolved values where placeholders were found.
     */
    public static Map<String, Object> convertAppSecrets(Map<String, Object> configMap) throws SecretConversionException {
        Map<String, Object> resolvedMap = new HashMap<>(configMap);

        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String value = String.valueOf(entry.getValue());
            try {
                String resolvedValue = resolveSecrets(value, configMap);
                if (!value.equals(resolvedValue)) {
                    resolvedMap.put(entry.getKey(), resolvedValue);
                }
            } catch (Exception e) {
                logger.error("Error resolving secrets for key: {}", entry.getKey(), e);
                throw new SecretConversionException("Failed to resolve secrets for key: " + entry.getKey(), e);
            }
        }

        return resolvedMap;
    }

    /**
     * Resolves secrets in a given value string by replacing placeholders
     * with their corresponding values from the config map.
     *
     * @param value The value string potentially containing placeholders.
     * @param configMap The configuration map to resolve secrets from.
     * @return The value with all placeholders replaced by their corresponding values.
     */
    private static String resolveSecrets(String value, Map<String, Object> configMap) {
        Matcher matcher = SECRET_PATTERN.matcher(value);
        StringBuffer resolvedValue = new StringBuffer();

        while (matcher.find()) {
            String secretKey = matcher.group(1);

            if (configMap.containsKey(secretKey)) {
                matcher.appendReplacement(resolvedValue, String.valueOf(configMap.get(secretKey)));
            } else {
                logger.warn("App secret not found for key: {}", secretKey);
                matcher.appendReplacement(resolvedValue, matcher.group(0));  // Keep original placeholder
            }
        }

        matcher.appendTail(resolvedValue);
        return resolvedValue.toString();
    }

    /**
     * Custom exception to handle errors during secret conversion.
     */
    public static class SecretConversionException extends Exception {
        public SecretConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
