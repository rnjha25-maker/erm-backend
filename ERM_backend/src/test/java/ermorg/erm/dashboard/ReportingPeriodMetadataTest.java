package ermorg.erm.dashboard;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static ermorg.erm.dashboard.ChartPeriod.YearType.*;

class ReportingPeriodMetadataTest {
    @Test void legacyOmissionDoesNotEraseExistingMetadata() {
        var period=ReportingPeriodMetadata.resolve(null,null,2026,FINANCIAL);
        assertThat(period.year()).isEqualTo(2026);assertThat(period.yearType()).isEqualTo(FINANCIAL);
        ReportingPeriodMetadata.resolve(null,null,null,null).validateFrequency(null,"Annual");
    }
    @Test void requiresPairAndPreventsMovingHistoryIntoAnotherYear() {
        assertThatIllegalArgumentException().isThrownBy(()->ReportingPeriodMetadata.resolve(2026,null,null,null));
        assertThatIllegalArgumentException().isThrownBy(()->ReportingPeriodMetadata.resolve(null,CALENDAR,null,null));
        assertThatIllegalArgumentException().isThrownBy(()->ReportingPeriodMetadata.resolve(2027,CALENDAR,2026,CALENDAR));
        assertThatIllegalArgumentException().isThrownBy(()->ReportingPeriodMetadata.resolve(2026,FINANCIAL,2026,CALENDAR));
        assertThatIllegalArgumentException().isThrownBy(()->ReportingPeriodMetadata.resolve(9999,CALENDAR,null,null));
        assertThat(ReportingPeriodMetadata.resolve(2026,CALENDAR,null,null).year()).isEqualTo(2026);
    }
    @Test void taggedEntriesUseOneMonthlyOrQuarterlyFrequency() {
        var period=ReportingPeriodMetadata.resolve(2026,CALENDAR,null,null);
        period.validateFrequency(" Monthly ","MONTHLY");period.validateFrequency("Quarterly","quarterly");
        assertThatIllegalArgumentException().isThrownBy(()->period.validateFrequency("Monthly","Quarterly"));
        assertThatIllegalArgumentException().isThrownBy(()->period.validateFrequency("Annual","Annual"));
        assertThatIllegalArgumentException().isThrownBy(()->period.validateFrequency(null,"Monthly"));
    }
}
