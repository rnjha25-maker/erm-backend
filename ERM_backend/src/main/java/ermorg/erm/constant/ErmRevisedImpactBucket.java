package ermorg.erm.constant;

public enum ErmRevisedImpactBucket {

	CATASTROPHIC("Catastrophic", 21, 25),
	MAJOR("Major", 17, 20),
	MODERATE("Moderate", 13, 16),
	MINOR("Minor", 9, 12),
	INSIGNIFICANT("Insignificant", 5, 8);

	private final String displayLabel;
	private final int minScore;
	private final int maxScore;

	ErmRevisedImpactBucket(String displayLabel, int minScore, int maxScore) {
		this.displayLabel = displayLabel;
		this.minScore = minScore;
		this.maxScore = maxScore;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	/** Returns null for scores outside 5-25, which are not reported. */
	public static ErmRevisedImpactBucket forScore(int score) {
		for (ErmRevisedImpactBucket bucket : values()) {
			if (score >= bucket.minScore && score <= bucket.maxScore) {
				return bucket;
			}
		}
		return null;
	}
}
