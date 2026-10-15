package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ermorg.erm.constant.ErmHeatmapLikelihoodBucket;
import ermorg.erm.constant.ErmHeatmapVelocityBucket;
import ermorg.erm.constant.ErmRevisedImpactBucket;
import ermorg.erm.constant.ErmRiskLevelBucket;
import ermorg.erm.dto.response.ErmHeatmapAxisBand;
import ermorg.erm.dto.response.ErmHeatmapCell;
import ermorg.erm.dto.response.ErmHeatmapLegendEntry;
import ermorg.erm.dto.response.ErmHeatmapRiskItem;
import ermorg.erm.dto.response.ErmRiskHeatmap;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.RiskReview;
import ermorg.erm.util.ErmDashboardValueNormalizer;
import lombok.RequiredArgsConstructor;

/**
 * Builds the erm-summary-v2 risk heatmap. A risk is scored from its latest review in the period
 * where possible and from its risk assessment otherwise, so nothing is silently dropped for want of
 * a review. Velocity never moves a risk between cells.
 */
@Service
@RequiredArgsConstructor
public class ErmRiskHeatmapBuilder {

	private static final String BASIS_RESIDUAL = "RESIDUAL";
	private static final String BASIS_INHERENT = "INHERENT";
	private static final String UNKNOWN = "UNKNOWN";
	private static final String NONE = "NONE";
	private static final String UNASSIGNED = "Unassigned";

	private final FieldMapperUtils fieldMapperUtils;

	/** Axes, 25 zero filled cells and both legends, for a period with no risks in scope. */
	public ErmRiskHeatmap buildEmpty() {
		return build(List.of(), Map.of(), Map.of());
	}

	public ErmRiskHeatmap build(List<Risk> risks, Map<Long, RiskReview> reviewByRiskId,
			Map<String, String> ownerLabels) {

		ErmRiskHeatmap heatmap = new ErmRiskHeatmap();
		heatmap.setLikelihoodAxis(buildLikelihoodAxis());
		heatmap.setImpactAxis(buildImpactAxis());
		heatmap.setCells(buildCells());
		heatmap.setTotalRisks(risks.size());

		Map<String, ErmHeatmapCell> cellByKey = new HashMap<>();
		for (ErmHeatmapCell cell : heatmap.getCells()) {
			cellByKey.put(cell.getKey(), cell);
		}

		Map<ErmRiskLevelBucket, Long> levelCounts = new EnumMap<>(ErmRiskLevelBucket.class);
		Map<ErmHeatmapVelocityBucket, Long> velocityCounts = new EnumMap<>(ErmHeatmapVelocityBucket.class);
		Map<String, ErmRiskLevelBucket> dominantByCellKey = new HashMap<>();

		for (Risk risk : risks) {
			ErmHeatmapRiskItem item = toRiskItem(risk, reviewByRiskId.get(risk.getId()), ownerLabels);
			if (item == null) {
				heatmap.setUnplottedRisks(heatmap.getUnplottedRisks() + 1);
				continue;
			}

			heatmap.getRisks().add(item);
			if (BASIS_RESIDUAL.equals(item.getScoreBasis())) {
				heatmap.setResidualScoredRisks(heatmap.getResidualScoredRisks() + 1);
			} else {
				heatmap.setInherentScoredRisks(heatmap.getInherentScoredRisks() + 1);
			}

			ErmHeatmapCell cell = cellByKey.get(item.getCellKey());
			cell.setCount(cell.getCount() + 1);
			cell.getRiskIds().add(item.getRiskId());

			ErmRiskLevelBucket level = ErmRiskLevelBucket.valueOf(item.getRiskLevelKey());
			levelCounts.merge(level, 1L, Long::sum);
			dominantByCellKey.merge(cell.getKey(), level,
					(a, b) -> a.getSeverity() >= b.getSeverity() ? a : b);

			if (item.getVelocityKey() != null) {
				velocityCounts.merge(ErmHeatmapVelocityBucket.valueOf(item.getVelocityKey()), 1L, Long::sum);
			}
			if (item.isFastMoving()) {
				cell.setFastMovingCount(cell.getFastMovingCount() + 1);
				cell.setVelocityHighlight(true);
				heatmap.setFastMovingRisks(heatmap.getFastMovingRisks() + 1);
			}
		}

		heatmap.setPlottedRisks(heatmap.getRisks().size());
		dominantByCellKey.forEach((cellKey, level) -> cellByKey.get(cellKey).setDominantRiskLevelKey(level.name()));
		heatmap.setRiskLevelLegend(buildRiskLevelLegend(levelCounts));
		heatmap.setVelocityLegend(buildVelocityLegend(velocityCounts));
		return heatmap;
	}

	/**
	 * Returns null when the risk cannot be placed, which happens when likelihood does not resolve to
	 * 1-5 or impact to 5-25. Velocity failing to resolve is not disqualifying.
	 */
	private ErmHeatmapRiskItem toRiskItem(Risk risk, RiskReview review, Map<String, String> ownerLabels) {

		RiskAssessment assessment = primaryAssessment(risk);

		Integer likelihoodScore = null;
		Integer impactScore = null;
		boolean residual = false;

		if (review != null) {
			likelihoodScore = likelihoodScore(review.getRevisedLikelihood());
			impactScore = impactScore(review.getReviseImpactScore());
			residual = likelihoodScore != null && impactScore != null;
		}
		if (!residual && assessment != null) {
			likelihoodScore = likelihoodScore(assessment.getLikelihood());
			impactScore = impactScore(assessment.getGrossImpactScore());
		}
		if (likelihoodScore == null || impactScore == null) {
			return null;
		}

		ErmHeatmapLikelihoodBucket likelihood = ErmHeatmapLikelihoodBucket.forScore(likelihoodScore);
		ErmRevisedImpactBucket impact = ErmRevisedImpactBucket.forScore(impactScore);
		ErmHeatmapVelocityBucket velocity = velocityBucket(review, assessment, residual);

		ErmHeatmapRiskItem item = new ErmHeatmapRiskItem();
		item.setRiskId(risk.getId());
		item.setRiskTitle(risk.getRisktitle());
		item.setCellKey(cellKey(likelihood.getPosition(), impact.getPosition()));

		item.setLikelihoodScore(likelihoodScore);
		item.setLikelihoodKey(likelihood.name());
		item.setLikelihoodLabel(likelihood.getDisplayLabel());

		item.setImpactScore(impactScore);
		item.setImpactKey(impact.name());
		item.setImpactLabel(impact.getDisplayLabel());

		if (velocity != null) {
			item.setVelocityScore(velocity.getPosition());
			item.setVelocityKey(velocity.name());
			item.setVelocityLabel(velocity.getDisplayLabel());
			item.setFastMoving(velocity.isFastMoving());
		}

		item.setRiskScore(likelihoodScore * impactScore);
		if (residual) {
			item.setResidualRiskScore(parseDouble(review.getResidualRiskScoreRange()));
		}

		String ratingLabel = ratingLabel(review, assessment, residual);
		item.setResidualRiskRatingKey(ErmDashboardValueNormalizer.ratingKey(ratingLabel));
		item.setResidualRiskRatingLabel(ratingLabel);

		ErmRiskLevelBucket level = ErmRiskLevelBucket.forValue(ratingLabel);
		if (level == null) {
			// Colour still has to come from somewhere, so fall back to the cell's own static level.
			level = ErmRiskLevelBucket.forCellScore(likelihood.getPosition() * impact.getPosition());
		}
		item.setRiskLevelKey(level.name());
		item.setScoreBasis(residual ? BASIS_RESIDUAL : BASIS_INHERENT);

		item.setCategoryKey(risk.getCategory() == null ? UNKNOWN : risk.getCategory().name());
		item.setOwnerDisplayLabel(ownerLabels.getOrDefault(ownerKey(risk), UNASSIGNED));
		return item;
	}

	/**
	 * Prefers the stored residual rating, falling back to the residual score bands and then to the
	 * assessment rating, reusing the same conversions the risk register and review responses apply.
	 */
	private String ratingLabel(RiskReview review, RiskAssessment assessment, boolean residual) {
		if (residual) {
			String label = fieldMapperUtils.resolveResidualRating(review.getResidualRiskRating(),
					review.getResidualRiskScoreRange());
			if (ErmDashboardValueNormalizer.hasValue(label)) {
				return label;
			}
		}
		if (assessment != null) {
			String label = fieldMapperUtils.resolveRatingLabel(assessment.getRiskRating());
			if (ErmDashboardValueNormalizer.hasValue(label)) {
				return label;
			}
		}
		return null;
	}

	private ErmHeatmapVelocityBucket velocityBucket(RiskReview review, RiskAssessment assessment, boolean residual) {
		if (residual) {
			return velocityBucket(review.getRevisedVelocity());
		}
		return assessment == null ? null : velocityBucket(assessment.getVelocity());
	}

	/** Score first, keyword only as a fallback, since these columns hold both forms. */
	private static Integer likelihoodScore(String value) {
		Integer score = ErmDashboardValueNormalizer.firstInteger(value);
		if (score != null && ErmHeatmapLikelihoodBucket.forScore(score) != null) {
			return score;
		}
		ErmHeatmapLikelihoodBucket bucket = ErmHeatmapLikelihoodBucket.forValue(value);
		return bucket == null ? null : bucket.getPosition();
	}

	/** Accepts 5-25, plus band labels, which resolve to the band midpoint. */
	private static Integer impactScore(String value) {
		Integer score = ErmDashboardValueNormalizer.firstInteger(value);
		if (score != null && ErmRevisedImpactBucket.forScore(score) != null) {
			return score;
		}
		ErmRevisedImpactBucket bucket = ErmDashboardValueNormalizer.matchLongestKeyword(value,
				ErmRevisedImpactBucket.values(), b -> new String[] { b.name() });
		return bucket == null ? null : bucket.getMidpointScore();
	}

	private static ErmHeatmapVelocityBucket velocityBucket(String value) {
		Integer score = ErmDashboardValueNormalizer.firstInteger(value);
		ErmHeatmapVelocityBucket bucket = score == null ? null : ErmHeatmapVelocityBucket.forScore(score);
		return bucket != null ? bucket : ErmHeatmapVelocityBucket.forValue(value);
	}

	private List<ErmHeatmapAxisBand> buildLikelihoodAxis() {
		List<ErmHeatmapAxisBand> axis = new ArrayList<>();
		for (ErmHeatmapLikelihoodBucket bucket : ErmHeatmapLikelihoodBucket.values()) {
			axis.add(new ErmHeatmapAxisBand(bucket.getPosition(), bucket.name(), bucket.getDisplayLabel(),
					bucket.getPosition(), bucket.getPosition()));
		}
		return axis;
	}

	private List<ErmHeatmapAxisBand> buildImpactAxis() {
		List<ErmHeatmapAxisBand> axis = new ArrayList<>();
		for (ErmRevisedImpactBucket bucket : ErmRevisedImpactBucket.values()) {
			axis.add(new ErmHeatmapAxisBand(bucket.getPosition(), bucket.name(), bucket.getDisplayLabel(),
					bucket.getMinScore(), bucket.getMaxScore()));
		}
		return axis;
	}

	/** Likelihood position descending, then impact position ascending, so cells[0] is L5-I1. */
	private List<ErmHeatmapCell> buildCells() {
		List<ErmHeatmapCell> cells = new ArrayList<>(25);
		for (ErmHeatmapLikelihoodBucket likelihood : ErmHeatmapLikelihoodBucket.values()) {
			for (int impactPosition = 1; impactPosition <= 5; impactPosition++) {
				ErmRevisedImpactBucket impact = impactBucketAt(impactPosition);
				int cellScore = likelihood.getPosition() * impactPosition;

				ErmHeatmapCell cell = new ErmHeatmapCell();
				cell.setKey(cellKey(likelihood.getPosition(), impactPosition));
				cell.setLikelihoodPosition(likelihood.getPosition());
				cell.setLikelihoodKey(likelihood.name());
				cell.setImpactPosition(impactPosition);
				cell.setImpactKey(impact.name());
				cell.setCellScore(cellScore);
				cell.setCellLevelKey(ErmRiskLevelBucket.forCellScore(cellScore).name());
				cells.add(cell);
			}
		}
		return cells;
	}

	private List<ErmHeatmapLegendEntry> buildRiskLevelLegend(Map<ErmRiskLevelBucket, Long> counts) {
		List<ErmHeatmapLegendEntry> legend = new ArrayList<>();
		int order = 1;
		for (ErmRiskLevelBucket bucket : ErmRiskLevelBucket.values()) {
			legend.add(new ErmHeatmapLegendEntry(bucket.name(), bucket.getDisplayLabel(), order++,
					counts.getOrDefault(bucket, 0L), false, bucket.getDescription()));
		}
		return legend;
	}

	private List<ErmHeatmapLegendEntry> buildVelocityLegend(Map<ErmHeatmapVelocityBucket, Long> counts) {
		List<ErmHeatmapLegendEntry> legend = new ArrayList<>();
		int order = 1;
		for (ErmHeatmapVelocityBucket bucket : ErmHeatmapVelocityBucket.values()) {
			legend.add(new ErmHeatmapLegendEntry(bucket.name(), bucket.getDisplayLabel(), order++,
					counts.getOrDefault(bucket, 0L), bucket.isFastMoving(),
					bucket.isFastMoving() ? "Fast moving risk" : ""));
		}
		return legend;
	}

	private static ErmRevisedImpactBucket impactBucketAt(int position) {
		for (ErmRevisedImpactBucket bucket : ErmRevisedImpactBucket.values()) {
			if (bucket.getPosition() == position) {
				return bucket;
			}
		}
		throw new IllegalArgumentException("No impact bucket at position " + position);
	}

	private static String cellKey(int likelihoodPosition, int impactPosition) {
		return "L" + likelihoodPosition + "-I" + impactPosition;
	}

	private static RiskAssessment primaryAssessment(Risk risk) {
		if (risk.getRiskAssessments() == null) {
			return null;
		}
		return risk.getRiskAssessments().stream()
				.filter(assessment -> !Boolean.TRUE.equals(assessment.getDeleted()))
				.findFirst()
				.orElse(null);
	}

	private static String ownerKey(Risk risk) {
		return risk.getRiskOwner() == null ? NONE : String.valueOf(risk.getRiskOwner().getId());
	}

	private static Double parseDouble(String value) {
		if (!ErmDashboardValueNormalizer.hasValue(value)) {
			return null;
		}
		try {
			double parsed = Double.parseDouble(value.trim());
			return Double.isFinite(parsed) ? parsed : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
