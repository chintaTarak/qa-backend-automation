package org.jarApiAutomation.utils;

import io.qameta.allure.Allure;
import io.restassured.response.Response;


public class AllureLogger
{

    public static void attachRequest(String endpoint, String headers, String body)
    {
        Allure.addAttachment("Request Endpoint", endpoint);
        Allure.addAttachment("Request Headers", headers);
        Allure.addAttachment("Request Body", body);
    }

    public static void attachResponse(Response response)
    {
        Allure.addAttachment("Response Status Code", String.valueOf(response.getStatusCode()));
        Allure.addAttachment("Response Body", response.asString());
    }

    public static void attachDBValue(String name, String value)
    {
        Allure.addAttachment("DB Value - " + name, value);
    }
}
