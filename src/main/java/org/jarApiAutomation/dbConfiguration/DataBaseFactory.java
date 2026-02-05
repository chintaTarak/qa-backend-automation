package org.jarApiAutomation.dbConfiguration;

import static org.jarApiAutomation.dbConfiguration.DBConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBaseFactory {

    // Singleton DB handlers (volatile ensures thread safety in multithread env)
    private static volatile MongoDBUtils digiGoldMongo;
    private static volatile MongoDBUtils tenantMongo;
    private static volatile MongoDBUtils changeJarMongo;
    private static volatile PostgresUtils digiPostgres;
    private static volatile PostgresUtils digiSDKPostgres;

    /**
     * Initializes DigiGold MongoDB connection (Singleton)
     *
     * @param uri MongoDB connection string
     */
    public static synchronized void initDigiGoldMongoDB(String uri) {
        if (digiGoldMongo == null) {
            digiGoldMongo = new MongoDBUtils(uri);
            log.info("[DB Init] DigiGold Mongo initialized");
        }
        digiGoldMongo.initializeClient();
    }

    /**
     * Initializes Tenant MongoDB connection (Singleton)
     *
     * @param uri MongoDB connection string
     */
    public static synchronized void initTenantMongoDB(String uri) {
        if (tenantMongo == null) {
            tenantMongo = new MongoDBUtils(uri);
            log.info("[DB Init] Tenant Mongo initialized");
        }
        tenantMongo.initializeClient();
    }

    /**
     * Initializes ChangeJar MongoDB connection (Singleton)
     *
     * @param uri MongoDB connection string
     */
    public static synchronized void initChangeJarMongoDB(String uri) {
        if (changeJarMongo == null) {
            changeJarMongo = new MongoDBUtils(uri);
            log.info("[DB Init] ChangeJar Mongo initialized");
        }
        changeJarMongo().initializeClient();
    }

    /** Initializes DigiGold PostgresDB connection (Singleton) */
    public static synchronized void initDigiPostgresDB(String url, String user, String pass) {
        if (digiPostgres == null) {
            digiPostgres = new PostgresUtils(url, user, pass);
            log.info("[DB Init] DigiGold Postgres initialized");
        }
        digiPostgres.getConnection();
    }

    /** Initializes DigiGold SDK PostgresDB connection (Singleton) */
    public static synchronized void initDigiSDKPostgresDB(String url, String user, String pass) {
        if (digiSDKPostgres == null) {
            digiSDKPostgres = new PostgresUtils(url, user, pass);
            log.info("[DB Init] DigiGold SDK Postgres initialized");
        }
        digiSDKPostgres.getConnection();
    }

    /**
     * @return DigiGold MongoDB client (already initialized)
     */
    public static MongoDBUtils digiGoldMongo() {
        return digiGoldMongo;
    }

    /**
     * @return Tenant MongoDB client (already initialized)
     */
    public static MongoDBUtils tenantMongo() {
        return tenantMongo;
    }

    /**
     * @return ChangeJar MongoDB client (already initialized)
     */
    public static MongoDBUtils changeJarMongo() {
        return changeJarMongo;
    }

    /**
     * @return DigiGold Postgres client (already initialized)
     */
    public static PostgresUtils digiPostgres() {
        return digiPostgres;
    }

    /**
     * @return DigiGold SDK Postgres client (already initialized)
     */
    public static PostgresUtils digiSDKPostgres() {
        return digiSDKPostgres;
    }

    /** Gracefully closes all initialized DB connections This should be called in @AfterSuite */
    public static void closeAllDBConnections() {
        if (digiGoldMongo != null) digiGoldMongo.closeConnection();
        if (tenantMongo != null) tenantMongo.closeConnection();
        if (changeJarMongo != null) changeJarMongo.closeConnection();
        if (digiPostgres != null) digiPostgres.disconnect();
        if (digiSDKPostgres != null) digiSDKPostgres.disconnect();
        log.info("[DB Close] All database connections closed");
    }

    public static void initializeDBConnections(String dbServer) {

        if (dbServer == null || dbServer.isBlank()) {
            dbServer = "changejar";
            log.info("No DB server specified. Defaulting to 'changejar'.");
        }

        switch (dbServer.toLowerCase()) {
            case "digigold":
                initDigiGoldMongoDB(DIGIGOLD_MONGO_DB_URL);
                initDigiPostgresDB(DIGIGOLD_PG_URL, DIGIGOLD_PG_USER, DIGIGOLD_PG_PWD);
                initDigiSDKPostgresDB(DIGIGOLDSDK_PG_URL, DIGIGOLD_PG_USER, DIGIGOLD_PG_PWD);
                initTenantMongoDB(TENANTS_MONGO_DB_URL);
                initChangeJarMongoDB(CHANGEJAR_MONGO_DB_URL);
                break;
            case "changejar":
                initChangeJarMongoDB(CHANGEJAR_MONGO_DB_URL);
                break;
            default:
                log.warn(
                        "Unknown DB server specified: {}. No DB connections initialized.",
                        dbServer);
        }
    }
}
