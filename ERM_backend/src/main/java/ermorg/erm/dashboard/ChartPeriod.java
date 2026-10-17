package ermorg.erm.dashboard;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

/** year is the starting year, e.g. FINANCIAL 2026 = Apr 2026 through Mar 2027. */
public record ChartPeriod(int year, YearType yearType, Integer quarter, LocalDate start, LocalDate end) {
    public enum YearType { CALENDAR, FINANCIAL }
    public enum Frequency { MONTHLY, QUARTERLY }
    public enum Indicator { KRI, KPI }
    public static ChartPeriod of(int year, YearType type, Integer quarter) {
        if (year < 1900 || year > 2200 || type == null || (quarter != null && (quarter < 1 || quarter > 4)))
            throw new IllegalArgumentException("Invalid year, yearType or quarter (1-4).");
        LocalDate first = LocalDate.of(year, type == YearType.CALENDAR ? 1 : 4, 1);
        LocalDate start = quarter == null ? first : first.plusMonths((quarter - 1L) * 3);
        return new ChartPeriod(year, type, quarter, start, start.plusMonths(quarter == null ? 12 : 3));
    }
    public List<String> labels(Frequency frequency) {
        if (frequency == Frequency.QUARTERLY)
            return quarter == null ? List.of("Q1", "Q2", "Q3", "Q4") : List.of("Q" + quarter);
        return IntStream.range(0, quarter == null ? 12 : 3).mapToObj(i -> start.plusMonths(i).toString().substring(0, 7)).toList();
    }
    public List<String> columns(Frequency frequency) {
        if (frequency == Frequency.QUARTERLY)
            return quarter == null ? List.of("q1", "q2", "q3", "q4") : List.of("q" + quarter);
        return IntStream.range(0, quarter == null ? 12 : 3)
            .mapToObj(i -> start.plusMonths(i).getMonth().name().toLowerCase(Locale.ROOT)).toList();
    }
}
