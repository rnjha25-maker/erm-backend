package ermorg.erm.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KpaKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.Company;
import ermorg.erm.model.KpaKpiReview;
import ermorg.erm.model.Organization;
import ermorg.erm.model.User;
import ermorg.erm.repository.KpaKpiReviewRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;

@Service
public class KpaKpiReviewService {

    @Autowired
    private KpaKpiReviewRepository kpaKpiReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldMapperUtils fieldMapperUtils;

    @Transactional
    public KpaKpiReviewResponseDTO save(KpaKpiReviewRequestDTO request) throws ResourceNotFoundException {
        Organization organization = getOrganizationContext();
        Company company = getCompanyContext();

        User owner = getActiveUser(resolveOwnerId(request), "No user found for selected owner.");
        User evaluationBy = getActiveUser(resolveEvaluationById(request), "No user found for selected evaluation by.");

        KpaKpiReview review;
        if (request.getKpaKpiReviewId() == 0) {
            review = new KpaKpiReview();
        } else {
            review = kpaKpiReviewRepository.getByOrgIdAndCompanyIdAndReviewId(
                    organization.getId(), company.getId(), request.getKpaKpiReviewId());
            if (review == null) {
                throw new ResourceNotFoundException("KpaKpiReview not found.");
            }
        }

        mapRequestToEntity(request, review);
        review.setOrganization(organization);
        review.setCompany(company);
        review.setOwner(owner);
        review.setKpiEvaluationBy(evaluationBy);
        review.setReporting(resolveOptionalUser(request.getReporting(), "No user found for selected reporting person."));
        review.setAnnualLossExpectancy(calculateAnnualLossExpectancy(
                request.getPotentialLossPercentage(),
                request.getYearlyFrequency()));

        KpaKpiReview saved = kpaKpiReviewRepository.save(review);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public KpaKpiReviewResponseDTO get(Long id) throws ResourceNotFoundException {
        Organization organization = getOrganizationContext();
        Company company = getCompanyContext();
        KpaKpiReview review = kpaKpiReviewRepository.getByOrgIdAndCompanyIdAndReviewId(
                organization.getId(), company.getId(), id);
        if (review == null) {
            throw new ResourceNotFoundException("No record found.");
        }
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<KpaKpiReviewResponseDTO> getAll(Pageable pageable, String status, String search)
            throws ResourceNotFoundException {
        Organization organization = getOrganizationContext();
        Company company = getCompanyContext();
        return kpaKpiReviewRepository.getByOrgIdAndCompanyId(
                        organization.getId(),
                        company.getId(),
                        normalize(status),
                        normalize(search),
                        pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void delete(Long id) throws ResourceNotFoundException {
        Organization organization = getOrganizationContext();
        Company company = getCompanyContext();
        KpaKpiReview review = kpaKpiReviewRepository.getByOrgIdAndCompanyIdAndReviewId(
                organization.getId(), company.getId(), id);
        if (review == null) {
            throw new ResourceNotFoundException("No record found.");
        }
        review.setDeleted(true);
        kpaKpiReviewRepository.save(review);
    }

    private Organization getOrganizationContext() throws ResourceNotFoundException {
        Organization organization = OrganizationContext.getOrganization();
        if (organization == null || organization.getId() == null) {
            throw new ResourceNotFoundException("Organization context not found.");
        }
        return organization;
    }

    private Company getCompanyContext() throws ResourceNotFoundException {
        Company company = CompanyContext.getCompany();
        if (company == null || company.getId() == null) {
            throw new ResourceNotFoundException("Company context not found.");
        }
        return company;
    }

    private User getActiveUser(long userId, String message) throws ResourceNotFoundException {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private BigDecimal calculateAnnualLossExpectancy(BigDecimal potentialLossPercentage, Integer yearlyFrequency) {
        if (potentialLossPercentage == null || yearlyFrequency == null) {
            return null;
        }
        return potentialLossPercentage
                .multiply(BigDecimal.valueOf(yearlyFrequency))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void mapRequestToEntity(KpaKpiReviewRequestDTO request, KpaKpiReview review) {
        review.setKpa(request.getKpa());
        review.setBusinessObjectives(request.getBusinessObjectives());
        review.setBusinessFunction(resolveFirstText(
                request.getBusinessFunction(), request.getDepartmentFunction(), request.getDepartmentName(),
                request.getDepartment()));
        review.setTarget(request.getTarget());
        review.setKeyPerformanceParameters(request.getKeyPerformanceParameters());
        review.setKeyPerformanceIndicator(request.getKeyPerformanceIndicator());
        review.setTypesOfKpi(request.getTypesOfKpi());
        review.setPerformanceIndicators(request.getPerformanceIndicators());
        review.setStakeholderDepartments(request.getStakeholderDepartments());
        review.setPerformanceToleranceMinValue(resolveDecimalAlias(
                request.getPerformanceToleranceMinValue(), request.getRiskToleranceRangeMinValue()));
        review.setPerformanceToleranceMaxValue(resolveDecimalAlias(
                request.getPerformanceToleranceMaxValue(), request.getRiskToleranceRangeMaxValue()));
        review.setTargets(request.getTargets());
        review.setActivities(request.getActivities());
        review.setThresholds(request.getThresholds());
        review.setPerformanceAppetite(resolveStringAlias(request.getPerformanceAppetite(), request.getRiskAppetite()));
        review.setEscalationMatrix(request.getEscalationMatrix());
        review.setLevelOfMeasurementLevel(resolveFirstText(
                request.getMeasurableParameters(), request.getUnitOfMeasurement()));
        review.setReportingFrequency(request.getReportingFrequency());
        review.setCurrency(request.getCurrency());
        review.setValueUnit(request.getValueUnit());
        review.setTargetValue(request.getTargetValue());
        review.setActualValue(request.getActualValue());
        review.setJanuary(request.getJanuary());
        review.setFebruary(request.getFebruary());
        review.setMarch(request.getMarch());
        review.setApril(request.getApril());
        review.setMay(request.getMay());
        review.setJune(request.getJune());
        review.setJuly(request.getJuly());
        review.setAugust(request.getAugust());
        review.setSeptember(request.getSeptember());
        review.setOctober(request.getOctober());
        review.setNovember(request.getNovember());
        review.setDecember(request.getDecember());
        review.setQ1(request.getQ1());
        review.setQ2(request.getQ2());
        review.setQ3(request.getQ3());
        review.setQ4(request.getQ4());
        review.setKpiType(request.getKpiType());
        review.setKraRating(request.getKraRating());
        review.setRiskAppetiteStatus(request.getRiskAppetiteStatus());
        review.setRiskAcceptanceLevel(request.getRiskAcceptanceLevel());
        review.setKpiEvaluationFrequency(request.getKpiEvaluationFrequency());
        review.setPotentialLossPercentage(request.getPotentialLossPercentage());
        review.setYearlyFrequency(request.getYearlyFrequency());
        review.setDueDate(request.getDueDate());
        review.setActualDate(request.getActualDate());
        review.setLastKpiEvaluationDate(request.getLastKpiEvaluationDate());
        review.setNextEvaluationDate(request.getNextEvaluationDate());
        review.setStatus(request.getStatus());
    }

    private BigDecimal resolveDecimalAlias(BigDecimal preferred, BigDecimal deprecated) {
        return preferred != null ? preferred : deprecated;
    }

    private String resolveStringAlias(String preferred, String deprecated) {
        return preferred != null ? preferred : deprecated;
    }

    private User resolveOptionalUser(Long userId, String errorMessage) throws ResourceNotFoundException {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userRepository.findById(userId)
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    }

    private String formatUserName(User user) {
        if (user == null) return null;
        if (user.getUserDetail() != null) {
            String fn = user.getUserDetail().getFirstName() == null ? "" : user.getUserDetail().getFirstName();
            String ln = user.getUserDetail().getLastName() == null ? "" : user.getUserDetail().getLastName();
            String full = (fn + " " + ln).trim();
            return full.isEmpty() ? user.getEmail() : full;
        }
        return user.getEmail();
    }

    private long resolveOwnerId(KpaKpiReviewRequestDTO request) {
        if (request.getOwnerId() > 0) {
            return request.getOwnerId();
        }
        if (request.getFunctionalOwner() != null && request.getFunctionalOwner() > 0) {
            return request.getFunctionalOwner();
        }
        return request.getBusinessFunctionalOwner() != null ? request.getBusinessFunctionalOwner() : 0;
    }

    private long resolveEvaluationById(KpaKpiReviewRequestDTO request) {
        if (request.getKpiEvaluationBy() > 0) {
            return request.getKpiEvaluationBy();
        }
        if (request.getEvaluationByNo() != null && request.getEvaluationByNo() > 0) {
            return request.getEvaluationByNo();
        }
        return request.getEvaluationBy() != null ? request.getEvaluationBy() : 0;
    }

    private String resolveFirstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public KpaKpiReviewResponseDTO toResponse(KpaKpiReview review) {
        KpaKpiReviewResponseDTO response = new KpaKpiReviewResponseDTO();
        response.setKpaKpiReviewId(review.getId());
        response.setKpa(review.getKpa());
        response.setKeyPerformanceArea(review.getKpa());
        response.setBusinessObjectives(review.getBusinessObjectives());
        response.setBusinessFunction(review.getBusinessFunction());
        response.setDepartmentFunction(review.getBusinessFunction());
        response.setDepartment(review.getBusinessFunction());

        if (review.getOwner() != null) {
            response.setOwnerId(review.getOwner().getId());
            response.setBusinessFunctionalOwner(review.getOwner().getId());
            // owner name/email for UI convenience
            if (review.getOwner().getUserDetail() != null) {
                String fn = review.getOwner().getUserDetail().getFirstName() == null ? "" : review.getOwner().getUserDetail().getFirstName();
                String ln = review.getOwner().getUserDetail().getLastName() == null ? "" : review.getOwner().getUserDetail().getLastName();
                String full = (fn + " " + ln).trim();
                response.setOwnerName(full.isEmpty() ? review.getOwner().getEmail() : full);
            } else {
                response.setOwnerName(review.getOwner().getEmail());
            }
            response.setFunctionalOwner(response.getOwnerName());
        }

        response.setTarget(review.getTarget());
        response.setKeyPerformanceParameters(review.getKeyPerformanceParameters());
        response.setKeyPerformanceIndicator(review.getKeyPerformanceIndicator());
        response.setKeyPerformanceIndicators(review.getKeyPerformanceIndicator());
        response.setTypesOfKpi(review.getTypesOfKpi());
        response.setPerformanceIndicators(review.getPerformanceIndicators());
        response.setStakeholderDepartments(review.getStakeholderDepartments());
        response.setPerformanceToleranceMinValue(review.getPerformanceToleranceMinValue());
        response.setPerformanceToleranceMaxValue(review.getPerformanceToleranceMaxValue());
        response.setRiskToleranceRangeMinValue(review.getPerformanceToleranceMinValue());
        response.setRiskToleranceRangeMaxValue(review.getPerformanceToleranceMaxValue());
        response.setTargets(review.getTargets());
        response.setActivities(review.getActivities());
        response.setThresholds(review.getThresholds());
        response.setPerformanceAppetite(review.getPerformanceAppetite());
        response.setRiskAppetite(review.getPerformanceAppetite());
        response.setEscalationMatrix(review.getEscalationMatrix());
        response.setMeasurableParameters(review.getLevelOfMeasurementLevel());
        // reporting: store both the user ID (for input re-population) and the resolved name
        if (review.getReporting() != null) {
            response.setReportingId(review.getReporting().getId());
            response.setReportingName(formatUserName(review.getReporting()));
        }
        response.setReporting(response.getReportingName());
        response.setReportingFrequency(review.getReportingFrequency());
        // Prefer enum label (valueUnit) for display; fall back to levelOfMeasurementLevel column if enum not set
        response.setUnitOfMeasurement(review.getValueUnit() != null ? review.getValueUnit().getLabel() : review.getLevelOfMeasurementLevel());
        response.setCurrency(review.getCurrency());
        response.setValueUnit(review.getValueUnit());
        // departmentName — resolve ID → name using the department repository
        String rawDept = review.getBusinessFunction() != null && !review.getBusinessFunction().isBlank()
                ? review.getBusinessFunction() : review.getStakeholderDepartments();
        response.setDepartmentName(fieldMapperUtils.resolveDepartmentFromObject(rawDept));
        response.setTargetValue(review.getTargetValue());
        response.setActualValue(review.getActualValue());
        response.setActuals(review.getActualValue());
        response.setJanuary(review.getJanuary());
        response.setFebruary(review.getFebruary());
        response.setMarch(review.getMarch());
        response.setApril(review.getApril());
        response.setMay(review.getMay());
        response.setJune(review.getJune());
        response.setJuly(review.getJuly());
        response.setAugust(review.getAugust());
        response.setSeptember(review.getSeptember());
        response.setOctober(review.getOctober());
        response.setNovember(review.getNovember());
        response.setDecember(review.getDecember());
        response.setQ1(review.getQ1());
        response.setQ2(review.getQ2());
        response.setQ3(review.getQ3());
        response.setQ4(review.getQ4());
        response.setMonthlyValues(formatValues(
                review.getJanuary(), review.getFebruary(), review.getMarch(), review.getApril(),
                review.getMay(), review.getJune(), review.getJuly(), review.getAugust(),
                review.getSeptember(), review.getOctober(), review.getNovember(), review.getDecember()));
        response.setQuarterlyValues(formatValues(review.getQ1(), review.getQ2(), review.getQ3(), review.getQ4()));
        response.setKpiType(review.getKpiType());
        response.setKraRating(review.getKraRating());
        response.setRiskAppetiteStatus(review.getRiskAppetiteStatus());
        response.setRiskAcceptanceLevel(review.getRiskAcceptanceLevel());

        if (review.getKpiEvaluationBy() != null) {
            response.setKpiEvaluationBy(review.getKpiEvaluationBy().getId());
            response.setEvaluationBy(review.getKpiEvaluationBy().getId());
            if (review.getKpiEvaluationBy().getUserDetail() != null) {
                String fn = review.getKpiEvaluationBy().getUserDetail().getFirstName() == null ? "" : review.getKpiEvaluationBy().getUserDetail().getFirstName();
                String ln = review.getKpiEvaluationBy().getUserDetail().getLastName() == null ? "" : review.getKpiEvaluationBy().getUserDetail().getLastName();
                String full = (fn + " " + ln).trim();
                response.setEvaluationByName(full.isEmpty() ? review.getKpiEvaluationBy().getEmail() : full);
            } else {
                response.setEvaluationByName(review.getKpiEvaluationBy().getEmail());
            }
            response.setEvaluationByNo(response.getEvaluationByName());
        }

        response.setKpiEvaluationFrequency(review.getKpiEvaluationFrequency());
        response.setPotentialLossPercentage(review.getPotentialLossPercentage());
        response.setYearlyFrequency(review.getYearlyFrequency());
        response.setAnnualLossExpectancy(review.getAnnualLossExpectancy());
        response.setDueDate(review.getDueDate() != null ? review.getDueDate().toString() : null);
        response.setActualDate(review.getActualDate() != null ? review.getActualDate().toString() : null);
        response.setLastKpiEvaluationDate(review.getLastKpiEvaluationDate() != null
                ? review.getLastKpiEvaluationDate().toString()
                : null);
        response.setNextEvaluationDate(review.getNextEvaluationDate() != null
                ? review.getNextEvaluationDate().toString()
                : null);
        response.setStatus(review.getStatus());
        return response;
    }

    private String formatValues(Object... values) {
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
