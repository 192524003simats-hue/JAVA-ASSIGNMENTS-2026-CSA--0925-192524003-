package parking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class OneDayPass implements ParkingPass {
    private final LocalDate validDate;

    public OneDayPass(LocalDate validDate) {
        this.validDate = Objects.requireNonNull(validDate, "validDate");
    }

    @Override
    public boolean covers(LocalDateTime entry, LocalDateTime exit) {
        return entry.toLocalDate().equals(validDate)
                && exit.toLocalDate().equals(validDate);
    }
}
