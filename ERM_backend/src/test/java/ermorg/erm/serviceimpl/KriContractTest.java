package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.*;
import ermorg.erm.repository.*;
import ermorg.erm.util.OrganizationContext;

class KriContractTest {
    @Test
    void gridAlwaysIncludesKriIdentityWithoutAnIdColumn() throws Exception {
        var service = new KripKpiRiskService();
        var reviews = mock(KriKpiRiskRepository.class);
        var mapper = mock(ermorg.erm.util.mapper.CustomResponseMapper.class);
        ReflectionTestUtils.setField(service, "kriKpiReskRepository", reviews);
        ReflectionTestUtils.setField(service, "customResponseMapper", mapper);
        ReflectionTestUtils.setField(service, "fieldMapperUtils", mock(FieldMapperUtils.class));
        var org = new Organization(); org.setId(1L);
        var review = new KriKpiReview(); review.setId(96105L);
        when(reviews.getByOrgId(1L)).thenReturn(List.of(review));
        when(mapper.map(eq("kriKpiReview"), eq(1L), any(), eq(true))).thenReturn(List.of());
        OrganizationContext.setOrganization(org);
        try {
            var fields = service.getAll().get(0);
            assertThat(fields).anySatisfy(f -> {
                assertThat(f.getFieldName()).isEqualTo("KRI ID");
                assertThat(f.getValue()).isEqualTo("96105");
            });
        } finally { OrganizationContext.clear(); }
    }
    @Test
    void saveAndGetPreserveDistinctValuesAndMeasurement() throws Exception {
        var service = new KripKpiRiskService();
        var reviews = mock(KriKpiRiskRepository.class);
        var risks = mock(RiskRepository.class);
        var users = mock(UserRepository.class);
        var assessments = mock(RiskAsessmentRepository.class);
        var fields = mock(FieldMapperUtils.class);
        ReflectionTestUtils.setField(service, "kriKpiReskRepository", reviews);
        ReflectionTestUtils.setField(service, "riskRepository", risks);
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "riskAsessmentRepository", assessments);
        ReflectionTestUtils.setField(service, "fieldMapperUtils", fields);
        var org = new Organization(); org.setId(1L);
        var risk = new Risk(); risk.setId(96099L);
        var user = new User(); user.setId(14336L); user.setDeleted(false);
        var ownerDetail = new UserDetail(); ownerDetail.setFirstName("Indira"); ownerDetail.setLastName("Gupta"); user.setUserDetail(ownerDetail);
        var reporter = new User(); reporter.setId(14662L);
        var reporterDetail = new UserDetail(); reporterDetail.setFirstName("Priya"); reporterDetail.setLastName("Sharma"); reporter.setUserDetail(reporterDetail);
        var evaluator = new User(); evaluator.setId(14506L);
        var evaluatorDetail = new UserDetail(); evaluatorDetail.setFirstName("Sonu"); evaluatorDetail.setLastName("Patel"); evaluator.setUserDetail(evaluatorDetail);
        var assessment = new RiskAssessment(); assessment.setId(96101L); assessment.setRisk(risk);
        when(risks.getRisksByOrgIdAndRiskId(1L, 96099L)).thenReturn(risk);
        when(users.findById(14336L)).thenReturn(Optional.of(user));
        when(users.findById(14662L)).thenReturn(Optional.of(reporter));
        when(users.findById(14506L)).thenReturn(Optional.of(evaluator));
        when(assessments.findByIdAndOrganizationIdAndDeletedFalse(96101L, 1L)).thenReturn(Optional.of(assessment));
        when(fields.resolveDepartmentFromObject("14413")).thenReturn("Legal & Compliance");
        when(reviews.save(any())).thenAnswer(i -> {
            KriKpiReview saved = i.getArgument(0); saved.setId(96105L);
            when(reviews.getByOrgIdAndKriId(1L, 96105L)).thenReturn(saved);
            when(reviews.findById(96105L)).thenReturn(Optional.of(saved));
            return saved;
        });
        var request = new KriKpiReviewRequestDTO();
        request.setRiskId(96099L); request.setRiskOwner(14336L);
        request.setReporting(14662L); request.setKriEvaluationBy(14506L); request.setRiskAssessmentId(96101L);
        request.setBusinessFunction("14413"); request.setBusinessObjectives("Environmental risk");
        request.setTarget("15"); request.setTargets("16"); request.setTargetValue("17");
        request.setActualValue("0"); request.setActuals("18");
        request.setRiskToleranceRangeMinValue("10"); request.setRiskToleranceRangeMaxValue("20");
        request.setUnitOfMeasurement("Ratios"); request.setCurrency("GBP");
        request.setMeasurableParameters("Emissions ratio");
        request.setRiskAppetiteStatus("Within Risk Appetite");
        request.setLastKriEvaluationDate("2026-09-16T17:10:05Z");
        OrganizationContext.setOrganization(org);
        try {
            var saved = service.save(request);
            var loaded = service.get(saved.getKriId());
            assertThat(loaded.getRiskAppetiteStatus()).isEqualTo("Within Risk Appetite");
            assertThat(loaded.getTarget()).isEqualTo("15");
            assertThat(loaded.getTargets()).isEqualTo("16");
            assertThat(loaded.getTargetValue()).isEqualTo("17");
            assertThat(loaded.getActualValue()).isEqualTo("0");
            assertThat(loaded.getActuals()).isEqualTo("18");
            assertThat(loaded.getDepartmentName()).isEqualTo("Legal & Compliance");
            assertThat(loaded.getRiskAssessmentId()).isEqualTo(96101L);
            assertThat(loaded.getRiskOwnerName()).isEqualTo("Indira Gupta");
            assertThat(loaded.getReportingName()).isEqualTo("Priya Sharma");
            assertThat(loaded.getKriEvaluationByName()).isEqualTo("Sonu Patel");
            assertThat(loaded.getMeasurableParameters()).isEqualTo("Emissions ratio");
            assertThat(Instant.parse(loaded.getLastKriEvaluationDate())).isEqualTo(Instant.parse("2026-09-16T17:10:05Z"));
            request.setKriId(96105L); request.setTarget("19");
            request.setRiskAppetiteStatus("Risk Tolerance Breached");
            service.save(request);
            assertThat(service.get(96105L).getRiskAppetiteStatus()).isEqualTo("Risk Tolerance Breached");
            assertThat(service.get(96105L).getTarget()).isEqualTo("19");
            assertThat(service.get(96105L).getReportingName()).isEqualTo("Priya Sharma");
        } finally { OrganizationContext.clear(); }
    }

    @Test
    void blankAssessmentAppetiteStatusFallsBackToRiskWithoutReplacingExplicitKriStatus() {
        var risk = new Risk(); risk.setId(1L); risk.setRiskAppetiteStatus("Within Risk Appetite");
        var assessment = new RiskAssessment(); assessment.setId(2L); assessment.setRiskAppetiteStatus("  ");
        var kri = new KriKpiReview(); kri.setId(3L); kri.setRisk(risk); kri.setRiskAssessment(assessment);
        assertThat(new KriKpiReviewResponseDTO(kri).getRiskAppetiteStatus()).isEqualTo("Within Risk Appetite");
        kri.setRiskAppetiteStatus("Risk Tolerance Breached");
        assertThat(new KriKpiReviewResponseDTO(kri).getRiskAppetiteStatus()).isEqualTo("Risk Tolerance Breached");
        kri.setRiskAppetiteStatus(null); risk.setRiskAppetiteStatus(null); assessment.setRiskAppetiteStatus(null);
        assertThat(new KriKpiReviewResponseDTO(kri).getRiskAppetiteStatus()).isEmpty();
    }

    @Test
    void responseDatesRoundTripAsIsoInstantsAndUnitsRemainDistinct() {
        var entity = new KriKpiReview(); entity.setId(1L);
        entity.setDueDate(Date.from(Instant.parse("2026-09-16T17:10:05Z")));
        entity.setUnitOfMeasurement("Ratios");
        entity.setValueUnit(ermorg.erm.constant.RiskValueUnit.values()[0]);
        var response = new KriKpiReviewResponseDTO(entity);
        assertThat(response.getUnitOfMeasurement()).isEqualTo("Ratios");
        assertThat(Instant.parse(response.getDueDate())).isEqualTo(Instant.parse("2026-09-16T17:10:05Z"));
    }
}
