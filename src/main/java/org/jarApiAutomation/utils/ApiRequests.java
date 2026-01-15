package org.jarApiAutomation.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.jarApiAutomation.data.common.RestRequest;

@Slf4j
public class ApiRequests {

    static {
        RestAssured.filters(new AllureRestAssured());
    }

    private RequestSpecification buildRequest(RestRequest req) {

        RequestSpecification request = RestAssured.given().relaxedHTTPSValidation();

        if (req.getHeaders() != null) {
            request.headers(req.getHeaders());
        }

        if (req.getQueryParams() != null) {
            request.queryParams(req.getQueryParams());
        }

        if (req.getBody() != null) {
            request.contentType(ContentType.JSON);
            request.body(req.getBody());
        }
        return request;
    }

    public Response get(RestRequest req) {
        log.info("GET Request: " + CommonSerializationUtil.writeString(req));
        Response response =
                buildRequest(req)
                        .when()
                        .get(req.getUrl())
                        .then()
                        .log()
                        .ifValidationFails()
                        .extract()
                        .response();
        log.info(
                "GET Response: Status Code: {}, Body: {}",
                response.getStatusCode(),
                CommonSerializationUtil.writeString(response.getBody().asString()));
        return response;
    }

    public Response post(RestRequest req) {
        log.info("POST Request: " + CommonSerializationUtil.writeString(req));
        Response response =
                buildRequest(req)
                        .when()
                        .post(req.getUrl())
                        .then()
                        .log()
                        .ifValidationFails()
                        .extract()
                        .response();
        log.info(
                "POST Response: Status Code: {}, Body: {}",
                response.getStatusCode(),
                CommonSerializationUtil.writeString(response.getBody().asString()));
        return response;
    }

    public Response put(RestRequest req) {
        log.info("PUT Request: " + CommonSerializationUtil.writeString(req));
        Response response =
                buildRequest(req)
                        .when()
                        .put(req.getUrl())
                        .then()
                        .log()
                        .ifValidationFails()
                        .extract()
                        .response();
        log.info(
                "PUT Response: Status Code: {}, Body: {}",
                response.getStatusCode(),
                CommonSerializationUtil.writeString(response.getBody().asString()));
        return response;
    }

    public Response delete(RestRequest req) {
        log.info("DELETE Request: " + CommonSerializationUtil.writeString(req));
        Response response =
                buildRequest(req)
                        .when()
                        .log()
                        .all()
                        .delete(req.getUrl())
                        .then()
                        .log()
                        .all()
                        .extract()
                        .response();
        log.info(
                "DELETE Response: Status Code: {}, Body: {}",
                response.getStatusCode(),
                CommonSerializationUtil.writeString(response.getBody().asString()));
        return response;
    }

    public Response patch(RestRequest req) {
        log.info("PATCH Request: " + CommonSerializationUtil.writeString(req));
        Response response =
                buildRequest(req)
                        .when()
                        .log()
                        .all()
                        .patch(req.getUrl())
                        .then()
                        .log()
                        .all()
                        .extract()
                        .response();
        log.info(
                "PATCH Response: Status Code: {}, Body: {}",
                response.getStatusCode(),
                CommonSerializationUtil.writeString(response.getBody().asString()));
        return response;
    }
}
