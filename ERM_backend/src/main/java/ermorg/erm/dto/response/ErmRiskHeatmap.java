package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Risk heatmap for the v2 dashboard. Likelihood (1-5) and impact (5-25) decide which cell a risk
 * lands in, residual risk rating decides its colour, and velocity (1-5) decides nothing about
 * position - it is returned separately as a fast moving overlay.
 */
@Data
public class ErmRiskHeatmap {

	/** Always 5 bands, position 5 down to 1. */
	private List<ErmHeatmapAxisBand> likelihoodAxis = new ArrayList<>();

	/** Always 5 bands, position 5 down to 1. */
	private List<ErmHeatmapAxisBand> impactAxis = new ArrayList<>();

	/** Always 25 cells: likelihood position descending, then impact position ascending. */
	private List<ErmHeatmapCell> cells = new ArrayList<>();

	private List<ErmHeatmapRiskItem> risks = new ArrayList<>();

	/** Always 5 entries, severity order. Drives cell and point colour. */
	private List<ErmHeatmapLegendEntry> riskLevelLegend = new ArrayList<>();

	/** Always 5 entries, speed order. Drives the separate fast moving overlay. */
	private List<ErmHeatmapLegendEntry> velocityLegend = new ArrayList<>();

	/** All risks in scope. Equals plottedRisks + unplottedRisks. */
	private long totalRisks;

	private long plottedRisks;

	/** Of the plotted risks, those scored from a review. */
	private long residualScoredRisks;

	/** Of the plotted risks, those scored from an assessment because no review exists. */
	private long inherentScoredRisks;

	/** Risks whose likelihood or impact fell outside the accepted range. */
	private long unplottedRisks;

	private long fastMovingRisks;
}
