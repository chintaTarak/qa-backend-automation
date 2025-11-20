package org.jarApiAutomation.utils;

import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class AllureReportUtil {

    /**
     *  Generating Allure report after suite
     */
    public static void generateAllureReport() {
        String pattern = "dd-MM-yyyy_HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String reportFolder = "allure-reports/allure-report_" + simpleDateFormat.format(new Date());

        try {
            CommonUtil.executeShellCmd("mkdir -p allure-reports");
            Thread.sleep(500);
            String command = String.format(
                    "allure generate allure-results --clean -o %s",
                    reportFolder
            );
            CommonUtil.executeShellCmd(command);
        } catch (Exception e) {
            log.error("Failed to generate Allure report: {}", e.getMessage(), e);
        }
    }

}
