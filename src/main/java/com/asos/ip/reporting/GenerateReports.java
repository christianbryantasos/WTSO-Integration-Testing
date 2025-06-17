package com.asos.ip.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.JsonFormatter;


import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateReports {
    private static ExtentSparkReporter spark;
    private static ExtentReports extent;
    private static final String REPORTS_DIR = "target/reports"; // Directory where JSON files are located
    public static void main(String[] args) throws IOException {

        // Set up the reporters
        ExtentSparkReporter spark = new ExtentSparkReporter("target/reports/CombinedReport/SparkReport.html");
        JsonFormatter jsonFormatter = new JsonFormatter("target/reports/CombinedReport/extent.json");

        // Create ExtentReports instance
        ExtentReports extent = new ExtentReports();

        // Scan the reports directory for JSON files
        Path reportsDir = Paths.get(REPORTS_DIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(reportsDir, "*.json")) {
            for (Path entry : stream) {
                // Add each JSON report file to the Extent report
                extent.createDomainFromJsonArchive(entry.toString());
                System.out.println("Adding report file: " + entry.toString()); // Log added files for debugging
            }
        } catch (IOException e) {
            System.err.println("Error reading report files: " + e.getMessage());
            throw e;
        }

        // Attach reporters
        extent.attachReporter(spark, jsonFormatter);

        // Finalize the report
        extent.flush();
        org.apache.logging.log4j.LogManager.shutdown(); // Properly shut down the logger
    }
}