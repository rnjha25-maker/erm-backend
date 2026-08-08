package ermorg.erm.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ermorg.erm.model.Company;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.ERMMaturityScore;

public final class ErmMaturityGroupingUtil {

	private ErmMaturityGroupingUtil() {
	}

	public static List<ERMMaturityAssessment> dedupeById(List<ERMMaturityAssessment> assessments) {
		if (assessments == null || assessments.isEmpty()) {
			return List.of();
		}
		return new ArrayList<>(assessments.stream()
				.collect(Collectors.toMap(ERMMaturityAssessment::getId, a -> a, (a, b) -> a))
				.values());
	}

	/**
	 * Groups assessments by ermMaturityId and returns entries sorted by group key.
	 * Null/blank ermMaturityId values are excluded.
	 */
	public static LinkedHashMap<String, List<ERMMaturityAssessment>> groupByErmMaturityId(
			List<ERMMaturityAssessment> assessments) {
		LinkedHashMap<String, List<ERMMaturityAssessment>> sorted = new LinkedHashMap<>();
		if (assessments == null || assessments.isEmpty()) {
			return sorted;
		}
		Map<String, List<ERMMaturityAssessment>> byGroup = assessments.stream()
				.filter(a -> a.getErmMaturityId() != null && !a.getErmMaturityId().isBlank())
				.collect(Collectors.groupingBy(ERMMaturityAssessment::getErmMaturityId));

		byGroup.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
		return sorted;
	}

	public static List<Long> firstRowDepartmentIds(List<ERMMaturityAssessment> group) {
		if (group == null || group.isEmpty()) {
			return List.of();
		}
		List<Long> ids = group.get(0).getDepartmentIds();
		return ids != null ? ids : List.of();
	}

	public static List<Long> activeDepartmentIds(List<Long> departmentIds) {
		if (departmentIds == null) {
			return List.of();
		}
		return departmentIds.stream().filter(id -> id != null && id != 0).distinct().sorted().toList();
	}

	public static boolean isCompanyWiseMaturity(List<Long> activeDeptIds) {
		return activeDeptIds == null || activeDeptIds.isEmpty();
	}

	public static String resolveFunctionDisplayLabel(List<Long> activeDeptIds, Map<String, String> departmentLabels) {
		if (activeDeptIds == null || activeDeptIds.isEmpty()) {
			return "";
		}
		Map<String, String> labels = departmentLabels != null ? departmentLabels : Map.of();
		return activeDeptIds.stream().map(id -> labels.getOrDefault(String.valueOf(id), String.valueOf(id)))
				.collect(Collectors.joining(", "));
	}

	public static String resolveDisplayLabel(String ermMaturityId, List<Long> activeDeptIds, Company company,
			Map<String, String> departmentLabels) {
		if (isCompanyWiseMaturity(activeDeptIds)) {
			return company != null && company.getName() != null ? company.getName() : ermMaturityId;
		}
		return resolveFunctionDisplayLabel(activeDeptIds, departmentLabels);
	}

	public static List<ERMMaturityScore> activeScores(ERMMaturityAssessment assessment) {
		if (assessment == null || assessment.getScores() == null) {
			return List.of();
		}
		return assessment.getScores().stream().filter(Objects::nonNull)
				.filter(s -> !Boolean.TRUE.equals(s.getDeleted())).toList();
	}

	public static List<ERMMaturityScore> activeScores(List<ERMMaturityAssessment> group) {
		if (group == null || group.isEmpty()) {
			return List.of();
		}
		List<ERMMaturityScore> scores = new ArrayList<>();
		for (ERMMaturityAssessment assessment : group) {
			scores.addAll(activeScores(assessment));
		}
		return scores;
	}

	/**
	 * Sums marksAchieved across all child scores in the group. Does not use weightageScore.
	 */
	public static BigDecimal totalScore(List<ERMMaturityAssessment> group) {
		return totalScoreFromScores(activeScores(group));
	}

	public static BigDecimal totalScoreFromScores(List<ERMMaturityScore> scores) {
		if (scores == null || scores.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return MaturityLevelResolver
				.sumMarksAchieved(scores.stream().map(ERMMaturityScore::getMarksAchieved).toList());
	}

	public static String maturityLabel(BigDecimal totalScore) {
		double score = totalScore != null ? totalScore.doubleValue() : 0;
		return MaturityLevelResolver.resolveMaturityLabel(score);
	}
}
