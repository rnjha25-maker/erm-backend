package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ErmRevisedImpactBucket;
import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.dto.response.ErmDashboardSummaryV2Response;
import ermorg.erm.dto.response.ErmFinancialExposureRow;
import ermorg.erm.dto.response.ErmGroupBreakdown;
import ermorg.erm.dto.response.NamedCount;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.RiskReview;
import ermorg.erm.repository.RiskReviewRepository;
import ermorg.erm.util.ErmDashboardPeriodBounds;
import ermorg.erm.util.ErmDashboardValueNormalizer;
import lombok.RequiredArgsConstructor;

/**
 * Builds the erm-summary-v2 cards. Every review driven number is taken from a single review per risk,
 * so a risk contributes at most 1 to one bucket of any given field.
 */
@Service
@RequiredArgsConstructor
public class ErmDashboardV2Service {

	private static final String UNKNOWN = "UNKNOWN";
	private static final String NONE = "NONE";

	private final RiskReviewRepository riskReviewRepository;

	@Transactional(readOnly = true)
	public ErmDashboardSummaryV2Response build(Long organizationId, List<Risk> risks,
			ErmDashboardPeriodBounds bounds, Map<String, String> companyLabels, Map<String, String> branchLabels,
			Map<String, String> functionLabels, Map<String, String> ownerLabels) {

		ErmDashboardSummaryV2Response response = new ErmDashboardSummaryV2Response();
		response.setHighRiskKris(risks.size());
		if (risks.isEmpty()) {
			return response;
		}

		Map<Long, RiskReview> reviewByRiskId = loadLatestReviewByRiskId(organizationId, risks, bounds);

		response.setErmRiskSummary(countBy(risks, ErmDashboardV2Service::categoryKey, Map.of()));
		response.setRiskSummary(countBy(risks, ErmDashboardV2Service::riskRegisterTypeKey, Map.of()));
		response.setRiskSummaryByImpact(buildImpactBuckets(risks, reviewByRiskId));
		response.setRiskRatingStatusOverview(buildResidualRatingCounts(risks, reviewByRiskId));

		response.setRiskRatingByLocation(
				buildRatingGroups(risks, reviewByRiskId, ErmDashboardV2Service::branchKey, branchLabels));
		response.setFunctionWiseRiskRatingSummary(
				buildRatingGroups(risks, reviewByRiskId, ErmDashboardV2Service::functionKey, functionLabels));
		response.setRiskRatingBycategory(
				buildRatingGroups(risks, reviewByRiskId, ErmDashboardV2Service::categoryKey, Map.of()));
		response.setRiskRatingByOwner(
				buildRatingGroups(risks, reviewByRiskId, ErmDashboardV2Service::ownerKey, ownerLabels));
		response.setRiskRatingByGroupCompany(
				buildCompanyRatingGroups(risks, reviewByRiskId, companyLabels));

		response.setFinancialExposureByRisk(buildFinancialExposure(risks, reviewByRiskId));

		populateDerivedCounts(response, risks, reviewByRiskId);
		return response;
	}

	private Map<Long, RiskReview> loadLatestReviewByRiskId(Long organizationId, List<Risk> risks,
			ErmDashboardPeriodBounds bounds) {

		List<Long> riskIds = risks.stream().map(Risk::getId).toList();
		List<RiskReview> reviews = riskReviewRepository.findForRiskRegister(organizationId, riskIds,
				bounds.getStartInclusive(), bounds.getEndInclusive());

		Map<Long, RiskReview> latestByRiskId = new HashMap<>();
		for (RiskReview review : reviews) {
			if (review.getRisk() == null) {
				continue;
			}
			Long riskId = review.getRisk().getId();
			RiskReview current = latestByRiskId.get(riskId);
			if (current == null || review.getId() > current.getId()) {
				latestByRiskId.put(riskId, review);
			}
		}
		return latestByRiskId;
	}

	private List<NamedCount> buildImpactBuckets(List<Risk> risks, Map<Long, RiskReview> reviewByRiskId) {
		Map<ErmRevisedImpactBucket, Long> counts = new EnumMap<>(ErmRevisedImpactBucket.class);
		for (Risk risk : risks) {
			RiskReview review = reviewByRiskId.get(risk.getId());
			if (review == null) {
				continue;
			}
			ErmRevisedImpactBucket bucket = ErmDashboardValueNormalizer.impactBucket(review.getReviseImpactScore());
			if (bucket != null) {
				counts.merge(bucket, 1L, Long::sum);
			}
		}

		List<NamedCount> buckets = new ArrayList<>();
		for (ErmRevisedImpactBucket bucket : ErmRevisedImpactBucket.values()) {
			buckets.add(new NamedCount(bucket.name(), counts.getOrDefault(bucket, 0L), bucket.getDisplayLabel()));
		}
		return buckets;
	}

	private List<NamedCount> buildResidualRatingCounts(List<Risk> risks, Map<Long, RiskReview> reviewByRiskId) {
		Map<String, Long> counts = new HashMap<>();
		for (Risk risk : risks) {
			String rating = residualRating(risk, reviewByRiskId);
			if (rating != null) {
				counts.merge(rating, 1L, Long::sum);
			}
		}
		return toNamedCounts(counts, Map.of());
	}

	private List<ErmGroupBreakdown> buildRatingGroups(List<Risk> risks, Map<Long, RiskReview> reviewByRiskId,
			Function<Risk, String> keyResolver, Map<String, String> labels) {

		Map<String, Map<String, Long>> ratingsByGroup = new HashMap<>();
		for (Risk risk : risks) {
			String groupKey = keyResolver.apply(risk);
			Map<String, Long> ratings = ratingsByGroup.computeIfAbsent(groupKey, k -> new HashMap<>());
			String rating = residualRating(risk, reviewByRiskId);
			if (rating != null) {
				ratings.merge(rating, 1L, Long::sum);
			}
		}

		List<ErmGroupBreakdown> groups = new ArrayList<>();
		ratingsByGroup.forEach((groupKey, ratings) -> {
			ErmGroupBreakdown group = new ErmGroupBreakdown();
			group.setKey(groupKey);
			group.setDisplayLabel(labels.getOrDefault(groupKey, groupKey));
			group.setTotal(ratings.values().stream().mapToLong(Long::longValue).sum());
			group.setBreakdown(toNamedCounts(ratings, Map.of()));
			groups.add(group);
		});
		groups.sort(Comparator.comparing(ErmGroupBreakdown::getKey, ErmDashboardV2Service::compareGroupKeys));
		return groups;
	}

	/** Company groups keep the full risk count as total, including risks without a review. */
	private List<ErmGroupBreakdown> buildCompanyRatingGroups(List<Risk> risks, Map<Long, RiskReview> reviewByRiskId,
			Map<String, String> companyLabels) {

		Map<String, Long> totals = new HashMap<>();
		for (Risk risk : risks) {
			totals.merge(companyKey(risk), 1L, Long::sum);
		}

		List<ErmGroupBreakdown> groups = buildRatingGroups(risks, reviewByRiskId, ErmDashboardV2Service::companyKey,
				companyLabels);
		groups.forEach(group -> group.setTotal(totals.getOrDefault(group.getKey(), group.getTotal())));
		return groups;
	}

	private List<ErmFinancialExposureRow> buildFinancialExposure(List<Risk> risks,
			Map<Long, RiskReview> reviewByRiskId) {

		List<ErmFinancialExposureRow> rows = new ArrayList<>();
		for (Risk risk : risks) {
			RiskReview review = reviewByRiskId.get(risk.getId());
			if (review == null || review.getAnnualLossExpectancy() == null || !isQuantitative(risk, review)) {
				continue;
			}
			rows.add(new ErmFinancialExposureRow(risk.getId(), risk.getRisktitle(), review.getAnnualLossExpectancy(),
					review.getCurrency()));
		}
		rows.sort(Comparator.comparing(ErmFinancialExposureRow::getAnnualLossExpectancy).reversed());
		return rows;
	}

	private void populateDerivedCounts(ErmDashboardSummaryV2Response response, List<Risk> risks,
			Map<Long, RiskReview> reviewByRiskId) {

		Map<String, Long> appetiteStatusCounts = new HashMap<>();
		Map<String, Long> acceptanceLevelCounts = new HashMap<>();
		long critical = 0;
		long withinAppetite = 0;
		long toleranceBreached = 0;
		long acceptable = 0;
		long unacceptable = 0;

		for (Risk risk : risks) {
			RiskReview review = reviewByRiskId.get(risk.getId());

			if (ErmDashboardValueNormalizer.isCriticalRating(residualRating(risk, reviewByRiskId))) {
				critical++;
			}

			String appetiteStatus = appetiteStatus(risk, review);
			if (appetiteStatus != null) {
				appetiteStatusCounts.merge(appetiteStatus, 1L, Long::sum);
				if (ErmDashboardValueNormalizer.isWithinAppetite(appetiteStatus)) {
					withinAppetite++;
				}
				if (ErmDashboardValueNormalizer.isToleranceBreached(appetiteStatus)) {
					toleranceBreached++;
				}
			}

			RiskAcceptanceLevel acceptanceLevel = acceptanceLevel(risk, review);
			if (acceptanceLevel != null) {
				acceptanceLevelCounts.merge(acceptanceLevel.name(), 1L, Long::sum);
				if (acceptanceLevel == RiskAcceptanceLevel.UNACCEPTABLE_RISK) {
					unacceptable++;
				} else {
					acceptable++;
				}
			}
		}

		response.setTotalKris(critical);
		response.setWithinRiskAppetite(withinAppetite);
		response.setRiskToleranceBreached(toleranceBreached);
		response.setOverdueEvaluations(acceptable);
		response.setUpcomingReview(unacceptable);
		response.setRiskAppetiteStatusCounts(toNamedCounts(appetiteStatusCounts, Map.of()));
		response.setRiskAcceptanceLevelCounts(toNamedCounts(acceptanceLevelCounts, Map.of()));
	}

	private String residualRating(Risk risk, Map<Long, RiskReview> reviewByRiskId) {
		RiskReview review = reviewByRiskId.get(risk.getId());
		if (review == null || !ErmDashboardValueNormalizer.hasValue(review.getResidualRiskRating())) {
			return null;
		}
		return review.getResidualRiskRating().trim();
	}

	private String appetiteStatus(Risk risk, RiskReview review) {
		if (review != null && ErmDashboardValueNormalizer.hasValue(review.getRiskAppetiteStatus())) {
			return review.getRiskAppetiteStatus().trim();
		}
		return ErmDashboardValueNormalizer.hasValue(risk.getRiskAppetiteStatus())
				? risk.getRiskAppetiteStatus().trim()
				: null;
	}

	private RiskAcceptanceLevel acceptanceLevel(Risk risk, RiskReview review) {
		if (review != null && review.getRiskAcceptanceLevel() != null) {
			return review.getRiskAcceptanceLevel();
		}
		return risk.getRiskAcceptanceLevel();
	}

	private boolean isQuantitative(Risk risk, RiskReview review) {
		if (ErmDashboardValueNormalizer.hasValue(review.getReviewType())) {
			return ErmDashboardValueNormalizer.isQuantitative(review.getReviewType());
		}
		if (risk.getRiskAssessments() == null) {
			return false;
		}
		return risk.getRiskAssessments().stream()
				.filter(assessment -> !assessment.getDeleted())
				.map(RiskAssessment::getRiskAnalysisType)
				.anyMatch(ErmDashboardValueNormalizer::isQuantitative);
	}

	private List<NamedCount> countBy(List<Risk> risks, Function<Risk, String> keyResolver,
			Map<String, String> labels) {

		Map<String, Long> counts = new HashMap<>();
		for (Risk risk : risks) {
			counts.merge(keyResolver.apply(risk), 1L, Long::sum);
		}
		return toNamedCounts(counts, labels);
	}

	private static List<NamedCount> toNamedCounts(Map<String, Long> counts, Map<String, String> labels) {
		Map<String, Long> sorted = new LinkedHashMap<>();
		counts.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getKey, ErmDashboardV2Service::compareGroupKeys))
				.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

		List<NamedCount> namedCounts = new ArrayList<>(sorted.size());
		sorted.forEach((key, count) -> namedCounts.add(new NamedCount(key, count, labels.getOrDefault(key, key))));
		return namedCounts;
	}

	/** Unassigned and unknown buckets sort last so charts start with real data. */
	private static int compareGroupKeys(String a, String b) {
		boolean aPlaceholder = NONE.equals(a) || UNKNOWN.equals(a);
		boolean bPlaceholder = NONE.equals(b) || UNKNOWN.equals(b);
		if (aPlaceholder != bPlaceholder) {
			return aPlaceholder ? 1 : -1;
		}
		return a.compareTo(b);
	}

	private static String categoryKey(Risk risk) {
		return risk.getCategory() == null ? UNKNOWN : risk.getCategory().name();
	}

	private static String riskRegisterTypeKey(Risk risk) {
		return ErmDashboardValueNormalizer.hasValue(risk.getRiskRegisterType()) ? risk.getRiskRegisterType().trim()
				: UNKNOWN;
	}

	private static String branchKey(Risk risk) {
		return risk.getBranchId() == null ? NONE : String.valueOf(risk.getBranchId());
	}

	private static String functionKey(Risk risk) {
		return risk.getFunction() == null ? NONE : String.valueOf(risk.getFunction());
	}

	private static String companyKey(Risk risk) {
		return risk.getCompanyId() == null ? NONE : String.valueOf(risk.getCompanyId());
	}

	private static String ownerKey(Risk risk) {
		return risk.getRiskOwner() == null ? NONE : String.valueOf(risk.getRiskOwner().getId());
	}
}
