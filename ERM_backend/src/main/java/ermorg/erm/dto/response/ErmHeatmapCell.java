package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** One of the 25 grid cells. All 25 are always emitted, zero filled, in a stable order. */
@Data
public class ErmHeatmapCell {

	private String key;
	private int likelihoodPosition;
	private String likelihoodKey;
	private int impactPosition;
	private String impactKey;

	/** Product of the two axis positions, 1-25. Grid geometry only. */
	private int cellScore;

	/** Static colour from the cell's own position. Always present, including on empty cells. */
	private String cellLevelKey;

	/** Highest residual level actually present in the cell; null when the cell is empty. */
	private String dominantRiskLevelKey;

	private long count;
	private long fastMovingCount;
	private boolean velocityHighlight;

	/** Lets the UI drive click through without scanning the risk list. */
	private List<Long> riskIds = new ArrayList<>();
}
