package FrameworkTests;

import com.asos.ip.helper.TestData.TestDataHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class TestDataHelperTests {

    TestDataHelper testDataHelper = new TestDataHelper();
    
    @Test
    @DisplayName("Testing fileExistsAtPath Failure")
    void testingFileExistsAtPathFailure() {

        assertTrue(testDataHelper.fileExistsAtPath("src/test/resources/TestDataHelper/testFile.json"));
    }

    @Test
    @DisplayName("Testing getFormattedZonedTime Method, Include Hours True")
    void testingGetFormattedZonedTimeMethodIncludeHoursTrue() {

        String testString = testDataHelper.getFormattedZonedTime(true);

        String regexForTestStringComparison = "\\d{4}/\\d{2}/\\d{2}/\\d{2}";

        Pattern pattern = Pattern.compile(regexForTestStringComparison);

        Matcher matcher = pattern.matcher(testString);

        Assertions.assertTrue(matcher.matches());
    }

    @Test
    @DisplayName("Testing getFormattedZonedTime Method, Include Hours False")
    void testingGetFormattedZonedTimeMethodIncludeHoursFalse() {

        String testString = testDataHelper.getFormattedZonedTime(false);

        String regexForTestStringComparison = "\\d{4}/\\d{2}/\\d{2}";

        Pattern pattern = Pattern.compile(regexForTestStringComparison);

        Matcher matcher = pattern.matcher(testString);

        Assertions.assertTrue(matcher.matches());
    }
    @Test
    @DisplayName("Testing blobLocationDateTimeBuilder Returns Expected")
    void testingBlobLocationDateTimeBuilderReturnsExpected() {

        String testString = testDataHelper.blobLocationDateTimeBuilder("ris-rdw", "testFile.json", true);

        String regexForTestStringComparison = "ris-rdw/\\d{4}/\\d{2}/\\d{2}/\\d{2}/testFile.json";

        Pattern pattern = Pattern.compile(regexForTestStringComparison);

        Matcher matcher = pattern.matcher(testString);

        Assertions.assertTrue(matcher.matches());
    }
}
