package base;

import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.dbConfiguration.MongoDBUtils;
import org.jarApiAutomation.utils.AllureReportUtil;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

@Slf4j
public class BaseTest
{
    @BeforeSuite(alwaysRun = true)
    public void setup()
    {
           log.info("======= Test Execution Started =======");
          MongoDBUtils. initializeMongoClient();
          log.info("[MongoDB]  Connection initialized successfully");
    }

    @AfterSuite(alwaysRun = true)
    public void closeConnection()
    {
        log.info("======= Test Execution Finished =======");
        /** Generate Allure report and open in browser * */
        AllureReportUtil.generateAllureReport();

        MongoDBUtils.closeConnection();
        log.info(" DB setup closed");

    }
}
