package ermorg.erm.constant;

import ermorg.erm.util.ErmDashboardValueNormalizer;

/**
 * The five colour bands of the risk heatmap. Residual risk rating is a free text column whose
 * vocabulary is wider than these five values, so stored ratings are folded in by keyword: "Very
 * High" reads as CRITICAL because it is the top band wherever it appears, and "Medium" reads as
 * MODERATE.
 * <p>
 * Matching is longest keyword first rather than declaration order, so "Very Low" cannot be captured
 * by the LOW keyword. Declaration order is display order.
 */
public enum ErmRiskLevelBucket {

	CRITICAL("Critical", 5, "Residual rating Critical or Very High", "CRITICAL", "VERYHIGH"),
	HIGH("High", 4, "Residual rating High", "HIGH"),
	MODERATE("Moderate", 3, "Residual rating Moderate or Medium", "MODERATE", "MEDIUM"),
	LOW("Low", 2, "Residual rating Low", "LOW"),
	VERY_LOW("Very Low", 1, "Residual rating Very Low", "VERYLOW");

	private final String displayLabel;
	private final int severity;
	private final String description;
	private final String[] keywords;

	ErmRiskLevelBucket(String displayLabel, int severity, String description, String... keywords) {
		this.displayLabel = displayLabel;
		this.severity = severity;
		this.description = description;
		this.keywords = keywords;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public int getSeverity() {
		return severity;
	}

	public String getDescription() {
		return description;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public static ErmRiskLevelBucket forValue(String value) {
		return ErmDashboardValueNormalizer.matchLongestKeyword(value, values(), ErmRiskLevelBucket::getKeywords);
	}

	/**
	 * Static colour of a grid cell, from the product of the two axis positions (1-25). This gives a
	 * conventional 5x5 matrix: 3 critical, 3 high, 5 moderate, 6 low and 8 very low cells.
	 */
	public static ErmRiskLevelBucket forCellScore(int cellScore) {
		if (cellScore >= 20) {
			return CRITICAL;
		}
		if (cellScore >= 13) {
			return HIGH;
		}
		if (cellScore >= 9) {
			return MODERATE;
		}
		if (cellScore >= 5) {
			return LOW;
		}
		return VERY_LOW;
	}
}
