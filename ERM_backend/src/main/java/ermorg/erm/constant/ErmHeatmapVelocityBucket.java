package ermorg.erm.constant;

import ermorg.erm.util.ErmDashboardValueNormalizer;

/**
 * Velocity band on the 1-5 scale where 5 is the fastest moving. Velocity is not a heatmap axis: it
 * never changes the cell a risk lands in and is carried only so the UI can overlay a separate
 * highlight on fast moving risks.
 */
public enum ErmHeatmapVelocityBucket {

	VERY_RAPID("Very Rapid", 5, true, "VERYRAPID"),
	RAPID("Rapid", 4, true, "RAPID"),
	MODERATE("Moderate", 3, false, "MODERATE", "MEDIUM"),
	SLOW("Slow", 2, false, "SLOW"),
	VERY_SLOW("Very Slow", 1, false, "VERYSLOW");

	private final String displayLabel;
	private final int position;
	private final boolean fastMoving;
	private final String[] keywords;

	ErmHeatmapVelocityBucket(String displayLabel, int position, boolean fastMoving, String... keywords) {
		this.displayLabel = displayLabel;
		this.position = position;
		this.fastMoving = fastMoving;
		this.keywords = keywords;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public int getPosition() {
		return position;
	}

	public boolean isFastMoving() {
		return fastMoving;
	}

	public String[] getKeywords() {
		return keywords;
	}

	/** Returns null outside 1-5. A risk with an unusable velocity is still plotted. */
	public static ErmHeatmapVelocityBucket forScore(int score) {
		for (ErmHeatmapVelocityBucket bucket : values()) {
			if (bucket.position == score) {
				return bucket;
			}
		}
		return null;
	}

	/** Longest keyword wins, so "Very Rapid" does not collapse into RAPID. */
	public static ErmHeatmapVelocityBucket forValue(String value) {
		return ErmDashboardValueNormalizer.matchLongestKeyword(value, values(),
				ErmHeatmapVelocityBucket::getKeywords);
	}
}
