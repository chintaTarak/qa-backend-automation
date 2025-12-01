package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.Data;
import org.jarApiAutomation.data.responseModel.auth.CommonResultModel;

import java.util.ArrayList;

@Data
public class UserResponse extends CommonResultModel {
    private boolean success;
    private DataObj data;

    @Data
    public static class DataObj {
        private String id;
        private String name;
        private String phoneNumber;
        private String countryCode;
        private String userRefId;
        private ArrayList<Object> currentBalance;
    }
}
