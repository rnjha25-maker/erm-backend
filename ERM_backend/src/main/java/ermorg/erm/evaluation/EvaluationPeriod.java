package ermorg.erm.evaluation;

import java.time.LocalDate;
import ermorg.erm.dashboard.ChartPeriod;

public record EvaluationPeriod(int year,ChartPeriod.YearType yearType,ChartPeriod.Frequency frequency,
        int number,LocalDate start,LocalDate end) {
    public static EvaluationPeriod of(int year,ChartPeriod.YearType type,ChartPeriod.Frequency frequency,int number) {
        var annual=ChartPeriod.of(year,type,null);
        if(frequency==null||number<1||number>(frequency==ChartPeriod.Frequency.MONTHLY?12:4))
            throw new IllegalArgumentException("Period must be 1-12 for MONTHLY or 1-4 for QUARTERLY.");
        int months=frequency==ChartPeriod.Frequency.MONTHLY?1:3;
        var start=annual.start().plusMonths((long)(number-1)*months);
        return new EvaluationPeriod(year,type,frequency,number,start,start.plusMonths(months));
    }
    public void validateDate(LocalDate date) {
        if(date==null||date.isBefore(start)||!date.isBefore(end))
            throw new IllegalArgumentException("effectiveDate must fall within the selected evaluation period.");
    }
}
