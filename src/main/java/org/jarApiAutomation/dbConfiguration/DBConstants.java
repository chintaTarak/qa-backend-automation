package org.jarApiAutomation.dbConfiguration;

public class DBConstants {
    public static final String CHANGEJAR_MONGO_DB_URL = "mongodb+srv://staging-qa:rfyhBgx5CLm6jEVX@cluster0-pl-0.7kwfi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    public static final String DIGIGOLD_MONGO_DB_URL = "mongodb://readOnly:GsKV2JPbL4ACU23@10.20.169.204:27017/digigold?authSource=digigold";
    public static final String TENANTS_MONGO_DB_URL ="mongodb://readUser:Z8V5VWBYSTxV78j@10.20.169.204:27017/tenants?authSource=tenants";
    public static final String DIGIGOLD_PG_URL ="jdbc:postgresql://db-staging-transactions.cnduq5fakbez.ap-south-1.rds.amazonaws.com:5432/digigold_sdk";
    public static final String DIGIGOLD_PG_USER ="digigold_sdk_read";
    public static final String DIGIGOLD_PG_PWD ="foreveryJar";

    public static final String AUTH_DB = "auth";
    public static final String SMS_DELIVERY_REPORTS = "smsDeliveryReports";
    public static final String FILTER_KEY = "number";
    public static final String DB_NAME = "main_db";
    public static final String MOBILE_NUMBER = "2000120005";
    public static final String COUNTRY_CODE_DB = "+91";
    public static final String SORT_FIELD = "createdAt";
}
