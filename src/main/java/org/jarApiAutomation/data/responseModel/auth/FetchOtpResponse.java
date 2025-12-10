package org.jarApiAutomation.data.responseModel.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@EqualsAndHashCode(callSuper = false)
public class FetchOtpResponse extends CommonResultModel {
    private String data;
}
