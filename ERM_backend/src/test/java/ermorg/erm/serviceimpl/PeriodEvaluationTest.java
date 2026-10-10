package ermorg.erm.serviceimpl;
import ermorg.erm.evaluation.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ermorg.erm.controller.PeriodEvaluationController;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;
import static ermorg.erm.dashboard.ChartPeriod.*;

class PeriodEvaluationTest {
    @Test void monthlyAndQuarterlyFinancialBoundaries() {
        var quarter=EvaluationPeriod.of(2026,YearType.FINANCIAL,Frequency.QUARTERLY,4);
        assertThat(quarter.start()).isEqualTo(LocalDate.of(2027,1,1));
        quarter.validateDate(LocalDate.of(2027,3,31));
        assertThatIllegalArgumentException().isThrownBy(()->quarter.validateDate(LocalDate.of(2027,4,1)));
        var month=EvaluationPeriod.of(2026,YearType.FINANCIAL,Frequency.MONTHLY,12);
        assertThat(month.start()).isEqualTo(LocalDate.of(2027,3,1));
        assertThatIllegalArgumentException().isThrownBy(()->EvaluationPeriod.of(2026,YearType.CALENDAR,Frequency.MONTHLY,13));
    }
    @Test void staleAndCompletedWritesRejected() {
        assertThatThrownBy(()->PeriodEvaluationService.draft(Map.of("state","COMPLETED","revision",2L),2)).hasMessageContaining("immutable");
        assertThatThrownBy(()->PeriodEvaluationService.draft(Map.of("state","DRAFT","revision",2L),1)).hasMessageContaining("reload");
    }
    @Test void invalidNumbersAndNestedPayloadsRejected() throws Exception {
        var json=new ObjectMapper();
        assertThatIllegalArgumentException().isThrownBy(()->PeriodEvaluationService.validateValue("actual_value",json.readTree("\"not numeric\"")));
        assertThatIllegalArgumentException().isThrownBy(()->PeriodEvaluationService.validateValue("annual_loss_expectancy",json.readTree("-1")));
        assertThatIllegalArgumentException().isThrownBy(()->PeriodEvaluationService.validateValue("per_of_potential_loss",json.readTree("101")));
        assertThatIllegalArgumentException().isThrownBy(()->PeriodEvaluationService.validateValue("yearly_frequency",json.readTree("1.5")));
        assertThatIllegalArgumentException().isThrownBy(()->PeriodEvaluationService.validateValue("status",json.readTree("{}")));
        PeriodEvaluationService.validateValue("actual_value",json.readTree("0"));
    }
    @Test void conflictsAndBadBindingHaveCorrectHttpStatus() throws Exception {
        var service=mock(PeriodEvaluationService.class);
        when(service.start(any())).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"duplicate"));
        var mvc=MockMvcBuilders.standaloneSetup(new PeriodEvaluationController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/period-evaluations/start").contentType("application/json").content("{\"module\":\"REVIEW\",\"sourceId\":1,\"year\":2026,\"yearType\":\"CALENDAR\",\"frequency\":\"QUARTERLY\",\"periodNumber\":2}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/period-evaluations/start").contentType("application/json").content("{\"module\":\"RISK\"}"))
            .andExpect(status().isBadRequest());
    }
}
