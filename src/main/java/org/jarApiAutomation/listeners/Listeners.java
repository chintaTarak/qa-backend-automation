package org.jarApiAutomation.listeners;

import io.qameta.allure.testng.AllureTestNg;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.utils.AllureReportUtil;
import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

@Slf4j
public class Listeners extends AllureTestNg implements IExecutionListener {

    /**
     * Suite-level start: runs before the entire suite starts
     */
    @Override
    public void onStart(ISuite suite) {
        super.onStart(suite);
        log.info("Starting Test Suite: {}", suite.getName());
    }

    /**
     * Suite-level finish: runs after the entire suite finishes
     */
    @Override
    public void onFinish(ISuite suite) {
        super.onFinish(suite);
        log.info("Finishing Test Suite: {}", suite.getName());
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

    @Override
    public void onExecutionFinish() {
        log.info("Execution finished. Generating Allure Report...");
        AllureReportUtil.generateAllureReport();
    }
}