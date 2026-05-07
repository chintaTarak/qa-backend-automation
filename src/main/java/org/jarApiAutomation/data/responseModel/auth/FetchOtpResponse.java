package org.jarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchOtpResponse extends CommonResultModel {
    private boolean success;
    private String data;
}
