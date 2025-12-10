package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;

@Data
public class DigiGoldCommonErrorResponse {
    private String errorCode;
    private String error;
    private Boolean success;
}
