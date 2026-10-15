package ermorg.erm.dto.response;

import lombok.Data;

/** One risk plotted on the heatmap. */
@Data
public class ErmHeatmapRiskItem {

	private Long riskId;
	private String riskTitle;
	private String cellKey;

	private Integer likelihoodScore;
	private String likelihoodKey;
	private String likelihoodLabel;

	private Integer impactScore;
	private String impactKey;
	private String impactLabel;

	/** Null when the stored velocity is missing or outside 1-5. The risk is still plotted. */
	private Integer velocityScore;
	private String velocityKey;
	private String velocityLabel;
	private boolean fastMoving;

	/** likelihoodScore * impactScore, 5-125. Always present. */
	private int riskScore;

	/** Stored residual score. Null on inherent basis, and on a different scale from riskScore. */
	private Double residualRiskScore;

	/** Normalized stored rating, which may fall outside the five legend keys. */
	private String residualRiskRatingKey;
	private String residualRiskRatingLabel;

	/** One of the five risk level keys. This is what drives colour. */
	private String riskLevelKey;

	/** RESIDUAL when scored from a review in the period, otherwise INHERENT. */
	private String scoreBasis;

	private String categoryKey;
	private String ownerDisplayLabel;
}
