package org.jarApiAutomation.data.responseModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResultModel {
    private int statusCode;
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
    private String error;
}