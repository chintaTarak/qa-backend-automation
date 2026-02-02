package org.jarApiAutomation.data.responseModel.goldSDK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class AutoPayInitiateResponse extends CommonResultModel {

    private boolean success;
    private DataObj data;

    @Data
    public static class DataObj {
        private String userId;
        private String autopayId;
        private String autopayRefId;
        private int amount;
        private int maxAmount;
        private String status;
        private String frequency;
        private long nextInstallmentTime;
        private String intentUrl;
    }
}
