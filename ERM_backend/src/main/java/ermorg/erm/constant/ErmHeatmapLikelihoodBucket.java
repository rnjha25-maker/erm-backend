package ermorg.erm.constant;

import ermorg.erm.util.ErmDashboardValueNormalizer;

/**
 * Likelihood axis of the dashboard risk heatmap, on the 1-5 scale where 5 is the most likely.
 * The stored column is free text, so a value is read as a score first and matched on keywords only
 * as a fallback for label style data.
 */
public enum ErmHeatmapLikelihoodBucket {

	LIKELY("Likely", 5, "LIKELY"),
	LESS_LIKELY("Less Likely", 4, "LESSLIKELY"),
	MODERATE("Moderate", 3, "MODERATE", "POSSIBLE"),
	UNLIKELY("Unlikely", 2, "UNLIKELY"),
	RARE("Rare", 1, "RARE");

	private final String displayLabel;
	private final int position;
	private final String[] keywords;

	ErmHeatmapLikelihoodBucket(String displayLabel, int position, String... keywords) {
		this.displayLabel = displayLabel;
		this.position = position;
		this.keywords = keywords;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public int getPosition() {
		return position;
	}

	public String[] getKeywords() {
		return keywords;
	}

	/** Returns null outside 1-5, which is the range the heatmap accepts. */
	public static ErmHeatmapLikelihoodBucket forScore(int score) {
		for (ErmHeatmapLikelihoodBucket bucket : values()) {
			if (bucket.position == score) {
				return bucket;
			}
		}
		return null;
	}

	/** Longest keyword wins, so "Less Likely" and "Unlikely" do not collapse into LIKELY. */
	public static ErmHeatmapLikelihoodBucket forValue(String value) {
		return ErmDashboardValueNormalizer.matchLongestKeyword(value, values(),
				ErmHeatmapLikelihoodBucket::getKeywords);
	}
}
