package org.jarApiAutomation.data.requestModel.digiGold;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryOrderRequest
{
    private String userId;
    private String merchantOrderId;
    private Product product;
    private BillingDetails billingDetails;
    private ShippingDetails shippingDetails;

    @Data
    @Builder
    public static class Product
    {
        private String skuId;
        private String quantity;
    }
    @Data
    @Builder
    public static class BillingDetails
    {
        private String name;
        private String phoneNumber;
        private Address address;
    }
    @Data
    @Builder
    public static class ShippingDetails
    {
        private String name;
        private String phoneNumber;
        private Address address;
    }
    @Data
    @Builder
    public static class Address
    {
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String pinCode;
        private String country;
    }
    /**
     * Static factory method for creating DeliveryOrderRequest
     */
    public static DeliveryOrderRequest createDeliveryOrder(String userId, String merchantOrderId, String skuId, String quantity, String name, String phoneNumber, Address address)
    {
        Product product = Product.builder()
                .skuId(skuId)
                .quantity(quantity)
                .build();

        BillingDetails billingDetails = BillingDetails.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .address(address)
                .build();

        ShippingDetails shippingDetails = ShippingDetails.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .address(address)
                .build();

        return DeliveryOrderRequest.builder()
                .userId(userId)
                .merchantOrderId(merchantOrderId)
                .product(product)
                .billingDetails(billingDetails)
                .shippingDetails(shippingDetails)
                .build();
    }
}
