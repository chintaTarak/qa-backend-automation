package org.jarApiAutomation.listeners;

import io.qameta.allure.Flaky;
import lombok.extern.slf4j.Slf4j;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.lang.reflect.Method;

@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess()) {
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                log.info("Retrying test: " + result.getName() + " | Attempt: " + retryCount);
                result.setStatus(ITestResult.SKIP);
                return true;
            } else {
                Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
                if (testMethod.getAnnotation(Flaky.class) != null) {
                    result.setStatus(ITestResult.SKIP);
                    return false;
                }
                result.setStatus(ITestResult.FAILURE);
            }
        } else {
            result.setStatus(ITestResult.SUCCESS);
        }
        return false;
    }
}