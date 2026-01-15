package org.jarApiAutomation.data.responseModel.goldSDK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResponse extends CommonResultModel {

    private boolean success;
    private DataObj data;

    @Data
    public static class DataObj {
        private String id;
        private String name;
        private String phoneNumber;
        private String countryCode;
        private String userRefId;
    }
}
