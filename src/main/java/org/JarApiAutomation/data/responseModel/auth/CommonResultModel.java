package org.JarApiAutomation.data.responseModel.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResultModel {
    private int statusCode;
    private String message;
}