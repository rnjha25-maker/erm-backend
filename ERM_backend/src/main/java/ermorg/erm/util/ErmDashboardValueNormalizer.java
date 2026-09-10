package ermorg.erm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ermorg.erm.constant.ErmRevisedImpactBucket;
import ermorg.erm.constant.ErmRiskPriorityBucket;

/**
 * Residual rating, appetite status and review type are free-text columns whose values differ between
 * screens and releases ("MEDIUM", "Very Low", "within-appetite", "Qualitative"). Classification is
 * therefore keyword based on a case and punctuation insensitive form of the stored value.
 */
public final class ErmDashboardValueNormalizer {

	private static final Pattern FIRST_INTEGER = Pattern.compile("\\d+");

	private ErmDashboardValueNormalizer() {
	}

	public static boolean hasValue(String value) {
		return value != null && !value.isBlank();
	}

	public static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value.toUpperCase().replaceAll("[^A-Z0-9]", "");
	}

	public static boolean isCriticalRating(String residualRiskRating) {
		return normalize(residualRiskRating).contains("CRITICAL");
	}

	public static boolean isWithinAppetite(String riskAppetiteStatus) {
		return normalize(riskAppetiteStatus).contains("WITHIN");
	}

	public static boolean isToleranceBreached(String riskAppetiteStatus) {
		String normalized = normalize(riskAppetiteStatus);
		return normalized.contains("BREACH") || normalized.contains("EXCEED") || normalized.contains("OUTSIDE");
	}

	public static boolean isQuantitative(String type) {
		return normalize(type).contains("QUANTITATIVE");
	}

	public static boolean isQualitative(String type) {
		return normalize(type).contains("QUALITATIVE");
	}

	public static ErmRiskPriorityBucket priorityBucket(String riskPriority) {
		return ErmRiskPriorityBucket.forValue(riskPriority);
	}

	/**
	 * Collapses casing and spacing variants of a rating into one bucket key, so "High", "HIGH" and
	 * "high" all become "HIGH". Returns null for blank and unassessed values.
	 */
	public static String ratingKey(String rating) {
		if (!hasValue(rating)) {
			return null;
		}
		String key = rating.trim().replaceAll("\\s+", " ").toUpperCase();
		return "UNASSESSED".equals(key) ? null : key;
	}

	/** Title cased display label for a rating key: "VERY HIGH" becomes "Very High". */
	public static String ratingLabel(String ratingKey) {
		if (!hasValue(ratingKey)) {
			return ratingKey;
		}
		StringBuilder label = new StringBuilder(ratingKey.length());
		for (String word : ratingKey.trim().split(" ")) {
			if (word.isEmpty()) {
				continue;
			}
			if (label.length() > 0) {
				label.append(' ');
			}
			label.append(Character.toUpperCase(word.charAt(0)))
					.append(word.substring(1).toLowerCase());
		}
		return label.toString();
	}

	/** Handles both plain scores ("18") and range style values ("17-20"). */
	public static ErmRevisedImpactBucket impactBucket(String reviseImpactScore) {
		Integer score = firstInteger(reviseImpactScore);
		return score == null ? null : ErmRevisedImpactBucket.forScore(score);
	}

	public static Integer firstInteger(String value) {
		if (!hasValue(value)) {
			return null;
		}
		Matcher matcher = FIRST_INTEGER.matcher(value);
		if (!matcher.find()) {
			return null;
		}
		try {
			return Integer.valueOf(matcher.group());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
