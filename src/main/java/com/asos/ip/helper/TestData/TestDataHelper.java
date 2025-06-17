package com.asos.ip.helper.TestData;

import com.asos.ip.helper.Constants.FrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDataHelper {
    private static final Logger logger = LoggerFactory.getLogger(TestDataHelper.class);


    /**
     * Checks if a file exists at the specified path.
     *
     * @param filePath The path to the file.
     * @return True if the file exists, false otherwise.
     */
    public Boolean fileExistsAtPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist at path: " + filePath);
        }
        System.out.println("File exists at path: " + filePath);
        return true;
    }

    /**
     * Checks if a string contains a dynamic date time string
     * @param string The string to check
     * @return True if the string contains a dynamic date time string, false otherwise
     */
    public Boolean doesStringContainDynamicDateTime(String string) {
        String literalPattern = "yyyy/MM/dd(/HH)?";

        Pattern pattern = Pattern.compile(literalPattern);
        Matcher matcher = pattern.matcher(string);

        return matcher.find();
    }

    public String returnStringWithFormatedDateTime(String stringToBeReplaced) {

        if(!doesStringContainDynamicDateTime(stringToBeReplaced)) {
            logger.info("No date-time pattern found in string: {}. Returning Original String", stringToBeReplaced);
            return stringToBeReplaced;
        }

        logger.info("Replacing date-time pattern with dynamic date-time in string: {}", stringToBeReplaced);

        return replaceDateTimePatternWithDynamicDateTime(stringToBeReplaced, returnFormatedDateTime(stringToBeReplaced));
    }


    public String returnFilePathFromInputResourcesDirectory(String fileName) {
        StringBuilder filePathBuilder = new StringBuilder();
        filePathBuilder.append(FrameworkConstants.DEFAULT_RESOURCES_INPUT_DIRECTORY).append(fileName);

        String filePath = filePathBuilder.toString();
        fileExistsAtPath(filePath);

        return filePath;
    }

    private String replaceDateTimePatternWithDynamicDateTime(String stringToBeReplaced, String currentDateTime) {
        String dateTimePattern = "\\byyyy/MM/dd(?:/HH)?\\b";  // Regex pattern

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(dateTimePattern);
        Matcher matcher = pattern.matcher(stringToBeReplaced);

        // Replace the matched date-time pattern with the replacement string
        return matcher.replaceAll(currentDateTime);
    }

    /**
     * Generates a formatted zoned time string based on the current time in the specified time zone.
     *
     * @param string The format string to use.
     * @return The formatted zoned time string.
     */
    public String returnFormatedDateTime(String string) {
        String dateTimePattern = extractDateTimePattern(string);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);

            ZoneId zoneId = ZoneId.of("Europe/London");
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId).minusHours(1);
            String formattedTime = zonedDateTime.format(formatter);
            logger.info("Generated formatted Zoned Time: {}", formattedTime);
            return formattedTime;
        } catch (Exception e) {
            logger.error("Error generating formatted Zoned Time: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extracts the date time pattern from a string.
     *
     * @param string The string to extract the pattern from.
     * @return The date time pattern.
     */
    public static String extractDateTimePattern(String string) {
        // Define the regex pattern for "yyyy/MM/dd" or "yyyy/MM/dd/HH" with escaped slashes

        String literalPattern = "yyyy/MM/dd(/HH)?";

        // Compile the regex pattern
        try {
            Pattern pattern = Pattern.compile(literalPattern);
            Matcher matcher = pattern.matcher(string);

            // If a match is found, return it
            if (matcher.find()) {
                return matcher.group();
            } else {
                throw new IllegalArgumentException("No date time pattern found in string: " + string);
            }
        } catch (Exception e) {
            // Print the error message to debug further
            System.out.println("Error compiling pattern: " + e.getMessage());
            throw e;  // Rethrow the exception after printing
        }
    }

    public static String[] splitStringAtLastSlashReturnAsArray(String valueToRemove) {
        int index = valueToRemove.lastIndexOf('/'); // Find the last occurrence of '/'

        if (index == -1) {
            // If no slash is found, return the valueToRemove as the second part and an empty first part
            return new String[] { "", valueToRemove };
        }

        // Split into two parts: before and after the last '/'
        String part1 = valueToRemove.substring(0, index); // Before the last '/'
        String part2 = valueToRemove.substring(index + 1); // After the last '/'

        return new String[] { part1, part2 };
    }


    /**
     * Builds a blob location string that includes the specified blob location,
     * file name, and a timestamp based on the current date and time.
     *
     * @param blobLocation The base blob location.
     * @param fileName The name of the file.
     * @param includeHour Whether to include the hour in the timestamp.
     * @return The constructed blob location string.
     */
    public String blobLocationDateTimeBuilder(String blobLocation, String fileName, boolean includeHour) {
        try {
            StringBuilder blobLocationDateTime = new StringBuilder();
            String formattedTime = getFormattedZonedTime(includeHour);

            blobLocationDateTime.append(blobLocation)
                    .append("/")
                    .append(formattedTime)
                    .append("/")
                    .append(fileName);

            logger.info("Built Blob location with timestamp: {}", blobLocationDateTime);
            return blobLocationDateTime.toString();
        } catch (Exception e) {
            logger.error("Error building Blob location with timestamp for file {}: {}", fileName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generates a formatted zoned time string based on the current time in the specified time zone.
     *
     * @param includeHour Whether to include the hour in the formatted time.
     * @return The formatted zoned time string.
     */
    public String getFormattedZonedTime(boolean includeHour) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            if (includeHour) {
                formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH");
            }
            ZoneId zoneId = ZoneId.of("Europe/London");
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId).minusHours(1);
            String formattedTime = zonedDateTime.format(formatter);
            logger.info("Generated formatted Zoned Time: {}", formattedTime);
            return formattedTime;
        } catch (Exception e) {
            logger.error("Error generating formatted Zoned Time: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String[] parseIgnoredFieldsCamelCase(String ignoredFields) {
        // Split the ignored fields by commas, convert each to camelCase, and return the result as an array
        return (ignoredFields != null && !ignoredFields.trim().isEmpty())
                ? Arrays.stream(ignoredFields.split(","))
                .map(String::trim)
                .map(this::toCamelCase) // Convert each field to camelCase
                .toArray(String[]::new)
                : new String[0]; // Return an empty array instead of null
    }
    public String[] parseIgnoredFields(String ignoredFields) {
        // Split the ignored fields by commas, convert each to camelCase, and return the result as an array
        return (ignoredFields != null && !ignoredFields.trim().isEmpty())
                ? Arrays.stream(ignoredFields.split(","))
                .map(String::trim)
                .toArray(String[]::new)
                : new String[0]; // Return an empty array instead of null
    }

    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Convert PascalCase or snake_case to camelCase properly
        StringBuilder camelCaseString = new StringBuilder();

        // Convert input to camelCase by detecting uppercase letters and underscores
        boolean isNextUpper = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (i == 0) {
                camelCaseString.append(Character.toLowerCase(c));
            } else if (c == '_' || c == ' ') {
                isNextUpper = true; // Capitalize the next character
            } else if (Character.isUpperCase(c) || isNextUpper) {
                camelCaseString.append(Character.toUpperCase(c));
                isNextUpper = false;
            } else {
                camelCaseString.append(Character.toLowerCase(c));
            }
        }

        return camelCaseString.toString();
    }
}
