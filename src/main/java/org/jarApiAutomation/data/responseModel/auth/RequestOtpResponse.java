package org.jarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestOtpResponse extends CommonResultModel {

    private DataResponse data;

    @Data
    public static class DataResponse {
        private String reqId;
        private int length;
    }
}
