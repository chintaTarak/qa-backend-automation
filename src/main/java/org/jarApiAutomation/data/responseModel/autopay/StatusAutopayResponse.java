package org.jarApiAutomation.data.responseModel.autopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusAutopayResponse extends CommonResultModel {

    private StatusData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusData {
        private String title;
        private String subTitle;
        private String description;
        private String status;
        private long startDate;
        private long endDate;
        private String recurringFrequency;
        private double recurringAmount;
        private String bankLogo;
        private String bankName;
        private String provider;
        private String upiApp;
        private String upiId;
        private String subscriptionId;

        private Object autopaySuccessData;
        private Object postSetupNarrativesVariant;
        private Object postSetupNarratives;
        private Object autoPayTxnDetails;
        private Long cancellationDate;
        private Object autopayAdvice;
        private Object trustedUsers;

        @JsonProperty("isPartOfDsFlowExperiment")
        private boolean partOfDsFlowExperiment;

        private boolean isReSetup;
        private int setupCount;
        private String couponCode;
        private Double offeredAmount;
        private String primaryLottieUrl;
        private String secondaryLottieUrl;
        private Object updateDailySavingsSuccessResp;
        private Object coinsCreditedDetails;
        private Object emergencyFundStatusResp;
        private Object streaksData;
        private Object festiveSavingsStatusResponse;
        private Object cta;
        private Object buttonList;
        private String subsSetupType;
        private boolean partOfUserStreaks;
        private boolean success;
    }
}
