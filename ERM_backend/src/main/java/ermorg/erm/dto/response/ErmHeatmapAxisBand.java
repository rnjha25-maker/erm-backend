package ermorg.erm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One band on either heatmap axis. Position 5 is always the most severe end. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErmHeatmapAxisBand {

	private int position;
	private String key;
	private String displayLabel;
	private Integer minScore;
	private Integer maxScore;
}
