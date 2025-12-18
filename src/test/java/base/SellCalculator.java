package base;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class SellCalculator
{
    /** ------------------ Sell By Amount ------------------ **/
    public  CalculationResult sellByAmount(BigDecimal amount, BigDecimal assetPrice)
    {
        BigDecimal quantity = amount.divide(assetPrice, 10, RoundingMode.DOWN);
        return new CalculationResult(quantity);
    }

    /** ------------------ Sell By Quantity ------------------ **/
    public  CalculationResult sellByQuantity(BigDecimal quantity, BigDecimal assetPrice)
    {
        BigDecimal amount = quantity.multiply(assetPrice)
                .setScale(10, RoundingMode.DOWN);
        return new CalculationResult(amount);
    }
}
