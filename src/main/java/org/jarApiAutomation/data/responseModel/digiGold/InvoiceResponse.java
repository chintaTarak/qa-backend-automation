package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse extends CommonResultModel
{
    private boolean success;
    private dataResult data;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class dataResult
    {
        private String invoiceLink;
    }
}
