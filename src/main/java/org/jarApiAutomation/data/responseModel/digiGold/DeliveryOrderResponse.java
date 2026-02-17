package org.jarApiAutomation.data.responseModel.digiGold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jarApiAutomation.data.responseModel.CommonResultModel;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrderResponse extends CommonResultModel
{
    private boolean success;
    private DataResult data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataResult
    {
        private String orderId;
        private String invoiceId;
        private String userId;
        private String status;
        private DeliveryTrackingDetails deliveryTrackingDetails;
        private Product products;
        private AddressDetails billingDetails;
        private AddressDetails shippingDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryTrackingDetails
    {
        private Integer estimatedDispatchDays;
        private String deliveryStatus;
        private String logisticPartner;
        private String trackingLink;
        private String awbId;
        private List<TrackingStatus> trackingStatus;
    }
    @Data
    public static class TrackingStatus
    {
        private String deliveryStatus;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product
    {
        private String skuId;
        private String name;
        private String materialType;
        private Double purityInPercent;
        private Integer carat;
        private Double weight;
        private String weightUOM;
        private String hsnCode;
        private PriceDetails priceDetails;
        private List<String> termsAndConditions;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDetails
    {
        private BigDecimal price;
        private BigDecimal makingCharges;
        private Double totalAmount;
        private BigDecimal taxAdded;
        private Double additionalChargesAdded;
        private Double netTotal;
        private List<ApplicableTax> applicableTaxes;
        private List<Object> additionalCharges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicableTax
    {
        private String name;
        private Double value;
        private String type;
        private Double tax;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDetails
    {
        private String name;
        private String phoneNumber;
        private Address address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address
    {
        private String address1;
        private String address2;
        private String pinCode;
        private String city;
        private String state;
        private String country;
    }
}
