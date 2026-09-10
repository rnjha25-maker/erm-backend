package ermorg.erm.constant;

import ermorg.erm.util.ErmDashboardValueNormalizer;

/**
 * Risk priority is a free text column, so the five dashboard buckets are matched on keywords.
 * Declaration order is the evaluation order: VERY_HIGH must be tested before HIGH and VERY_LOW
 * before LOW, otherwise "VERYHIGH" would match the HIGH keyword first.
 */
public enum ErmRiskPriorityBucket {

	VERY_HIGH("Very High", "VERYHIGH"),
	HIGH("High", "HIGH"),
	MODERATE("Moderate", "MODERATE", "MEDIUM"),
	LOW("Low", "LOW"),
	VERY_LOW("Very Low", "VERYLOW");

	private final String displayLabel;
	private final String[] keywords;

	ErmRiskPriorityBucket(String displayLabel, String... keywords) {
		this.displayLabel = displayLabel;
		this.keywords = keywords;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	/** Returns null for blank values and for values that match none of the five buckets. */
	public static ErmRiskPriorityBucket forValue(String value) {
		String normalized = ErmDashboardValueNormalizer.normalize(value);
		if (normalized.isEmpty()) {
			return null;
		}
		for (ErmRiskPriorityBucket bucket : values()) {
			for (String keyword : bucket.keywords) {
				if (normalized.contains(keyword)) {
					return bucket;
				}
			}
		}
		return null;
	}
}
