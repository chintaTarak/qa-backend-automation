package base;

import lombok.extern.slf4j.Slf4j;
import static org.jarApiAutomation.dbConfiguration.DataBaseFactory.*;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

@Slf4j
public class BaseTest
{
    @BeforeSuite(alwaysRun = true)
    @Parameters("dbServer")
    public void setup(@Optional("changejar") String dbServer)
    {
        log.info("======= Test Execution Started =======");
        initializeDBConnections(dbServer);
        log.info("[MongoDB]  Connection initialized successfully");
    }

    @AfterSuite(alwaysRun = true)
    public void closeConnection()
    {
        log.info("======= Test Execution Finished =======");
        closeAllDBConnections();
    }
}
