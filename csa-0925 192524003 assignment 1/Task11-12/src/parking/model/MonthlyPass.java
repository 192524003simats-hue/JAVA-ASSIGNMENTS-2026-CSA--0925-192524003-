package parking.model;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

public final class MonthlyPass implements ParkingPass {
    private final YearMonth validMonth;

    public MonthlyPass(YearMonth validMonth) {
        this.validMonth = Objects.requireNonNull(validMonth, "validMonth");
    }

    @Override
    public boolean covers(LocalDateTime entry, LocalDateTime exit) {
        return YearMonth.from(entry).equals(validMonth)
                && YearMonth.from(exit).equals(validMonth);
    }
}
