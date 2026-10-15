package ermorg.erm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One legend row. Shared by the risk level legend, which drives cell colour, and the velocity
 * legend, which drives the separate fast moving overlay. Keys are semantic only: the palette lives
 * in the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErmHeatmapLegendEntry {

	private String key;
	private String displayLabel;
	private int order;
	private long count;
	/** Velocity legend only: true for the bands treated as fast moving. */
	private boolean highlight;
	private String description;
}
