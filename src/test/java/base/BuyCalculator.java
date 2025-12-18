package base;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class BuyCalculator
{
    private static final BigDecimal GST_PERCENT = new BigDecimal("1.50");


    private static BigDecimal calculateCGST(BigDecimal assetPrice)
    {
        return assetPrice.multiply(GST_PERCENT)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateSGST(BigDecimal assetPrice)
    {
        return assetPrice.multiply(GST_PERCENT)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTaxInclusiveRate(BigDecimal assetPrice)
    {
        BigDecimal cgst = calculateCGST(assetPrice);
        BigDecimal sgst = calculateSGST(assetPrice);
        return assetPrice.add(cgst).add(sgst);
    }

    /** ------------------ Buy by Amount ------------------ **/
    public  CalculationResult buyByAmount(BigDecimal amount, BigDecimal assetPrice) {
        BigDecimal taxInclusiveRate = calculateTaxInclusiveRate(assetPrice);
        BigDecimal quantity = amount.divide(taxInclusiveRate, 10, RoundingMode.DOWN);
        return new CalculationResult(quantity);
    }

    /** ------------------ Buy by Quantity ------------------ **/
    public  CalculationResult buyByQuantity(BigDecimal quantity, BigDecimal assetPrice) {
        BigDecimal taxInclusiveRate = calculateTaxInclusiveRate(assetPrice);
        BigDecimal amount = quantity.multiply(taxInclusiveRate)
                .setScale(6, RoundingMode.DOWN);
        return new CalculationResult(amount);
    }

}
