package org.jarApiAutomation.listeners;

import io.qameta.allure.testng.AllureTestNg;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.dbConfiguration.MongoDBUtils;
import org.jarApiAutomation.utils.AllureReportUtil;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

@Slf4j
public class Listeners extends AllureTestNg {

    /**
     * Suite-level start: runs before the entire suite starts
     */
    @Override
    public void onStart(ISuite suite) {
        log.info("Starting Test Suite: {}", suite.getName());
        try {
            // Initialize MongoDB connection
            MongoDBUtils.getClient();
            log.info("Mongo Db Connection Initialized");
        } catch (Exception e) {
            log.error("Error initializing suite setup: {}", e.getMessage(), e);
            throw new RuntimeException("Suite initialization failed", e);
        }
    }

    /**
     * Suite-level finish: runs after the entire suite finishes
     */
    @Override
    public void onFinish(ISuite suite) {
        log.info("Finishing Test Suite: {}", suite.getName());
        try {
            // Clean up MongoDB connection
            MongoDBUtils.closeConnection();
            log.info("MongoDB connection closed successfully");
        } catch (Exception e) {
            log.error("Error closing MongoDB connection: {}", e.getMessage(), e);
        }
        try {
            // Allure Report Generation
            Thread.sleep(8000);
            AllureReportUtil.generateAllureReport();
            log.info("Report Generation Correctly");
        } catch (Exception e) {
            log.error("Error generating Allure report: {}", e.getMessage(), e);
        }
    }

    /**
     * Test-level start: before any test method in a <test> tag starts
     */
    @Override
    public void onStart(ITestContext context) {
        super.onStart(context);
        log.info("Starting Test Context: {}", context.getName());
    }

    /**
     * Test method started: before each test method starts
     */
    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
        log.info("Test Started: {}", result.getName());
    }

    /**
     * Test method success
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result);
        log.info("Test Passed: {}", result.getName());
        onTestEnd(result);
    }

    /**
     * Test method failure
     */
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);
        log.error("Test Failed: {}", result.getName());
        if (result.getThrowable() != null) {
            log.error("Error: ", result.getThrowable());
        }
        onTestEnd(result);
    }

    /**
     * Common hook for ending a test method
     */
    private void onTestEnd(ITestResult result) {
        log.info("Test Status: {}", result.getStatus());
        log.info("Test Ended: {}", result.getName());
        log.info("---------------------------------------");
    }
}