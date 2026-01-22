package org.jarApiAutomation.data.responseModel.goldSDK;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
public class UploadResponse extends CommonResultModel {
    private DataObj data;

    @Data
    public static class DataObj {
        private String preSignedUrlPath;
        private String documentImageId;
    }
}
