package org.JarApiAutomation.data.common;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class RestRequest {
    private String url;
    private Map<String, String> headers;
    private Map<String, ?> queryParams;
    private Object body;
}
