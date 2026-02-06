package org.jarApiAutomation.data.responseModel.digiGold;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@EqualsAndHashCode(callSuper = true)
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
        private List<CurrentBalance> currentBalance;
    }
    @Data
    public static class CurrentBalance
    {
        private String materialCode;
        private BigDecimal quantity;
    }

}
