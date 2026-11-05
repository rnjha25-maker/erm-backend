package ermorg.erm.dashboard;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.model.*;
import ermorg.erm.repository.*;
import ermorg.erm.serviceimpl.KpaKpiReviewService;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.dto.riskDTO.KpaKpiReviewRequestDTO;
import ermorg.erm.util.*;

class KpiReportingPeriodTest {
    @Test void saveGetAndLegacyUpdatePreserveYearAndActuals() throws Exception {
        var service=new KpaKpiReviewService();var reviews=mock(KpaKpiReviewRepository.class);
        var users=mock(UserRepository.class);
        ReflectionTestUtils.setField(service,"kpaKpiReviewRepository",reviews);
        ReflectionTestUtils.setField(service,"userRepository",users);
        ReflectionTestUtils.setField(service,"fieldMapperUtils",mock(FieldMapperUtils.class));
        var org=new Organization();org.setId(10L);var company=new Company();company.setId(20L);
        var owner=new User();owner.setId(30L);
        when(users.findById(30L)).thenReturn(Optional.of(owner));
        when(reviews.save(any())).thenAnswer(call->{
            KpaKpiReview value=call.getArgument(0);value.setId(40L);
            when(reviews.getByOrgIdAndCompanyIdAndReviewId(10L,20L,40L)).thenReturn(value);return value;
        });
        var request=new KpaKpiReviewRequestDTO();request.setOwnerId(30L);request.setKpiEvaluationBy(30L);
        request.setReportingYear(2026);request.setReportingYearType(ChartPeriod.YearType.FINANCIAL);
        request.setReportingFrequency("QUARTERLY");request.setKpiEvaluationFrequency("QUARTERLY");
        request.setTargetValue(new BigDecimal("100"));request.setQ1(new BigDecimal("90"));
        OrganizationContext.setOrganization(org);CompanyContext.setCompany(company);
        try {
            assertThat(service.save(request).getReportingYear()).isEqualTo(2026);
            request.setKpaKpiReviewId(40L);request.setReportingYear(null);request.setReportingYearType(null);
            request.setQ2(new BigDecimal("95"));service.save(request);
            var loaded=service.get(40L);
            assertThat(loaded.getReportingYearType()).isEqualTo(ChartPeriod.YearType.FINANCIAL);
            assertThat(loaded.getReportingYear()).isEqualTo(2026);
            assertThat(loaded.getTargetValue()).isEqualByComparingTo("100");
            assertThat(loaded.getQ1()).isEqualByComparingTo("90");
            assertThat(loaded.getQ2()).isEqualByComparingTo("95");
        } finally {OrganizationContext.clear();CompanyContext.clear();}
    }
}
