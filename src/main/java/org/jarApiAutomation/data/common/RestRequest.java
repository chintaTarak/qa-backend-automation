package org.jarApiAutomation.data.common;

import java.util.Map;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
