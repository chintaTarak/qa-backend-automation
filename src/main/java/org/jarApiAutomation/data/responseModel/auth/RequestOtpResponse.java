package org.jarApiAutomation.data.responseModel.auth;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;
import org.jarApiAutomation.data.responseModel.auth.responseBuyGold.BuyGoldResponse;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestOtpResponse extends CommonResultModel {
 private Boolean success;
 private DaResponse data;
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DaResponse{
     private String reqId;
     private int length;
 }


}
