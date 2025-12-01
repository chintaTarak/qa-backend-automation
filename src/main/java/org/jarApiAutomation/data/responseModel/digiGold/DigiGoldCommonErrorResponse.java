package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.auth.CommonResultModel;

@Data
public class DigiGoldCommonErrorResponse {
    private String errorCode;
    private String error;
    private Boolean success;
}
