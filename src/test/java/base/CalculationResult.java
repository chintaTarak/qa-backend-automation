package base;

import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class CalculationResult
{
    private BigDecimal fourDecimal;
    private BigDecimal sixDecimal;
    public CalculationResult(BigDecimal value)
    {
        this.fourDecimal = value.setScale(4, RoundingMode.DOWN);
        this.sixDecimal = value.setScale(6, RoundingMode.DOWN);
    }
}
