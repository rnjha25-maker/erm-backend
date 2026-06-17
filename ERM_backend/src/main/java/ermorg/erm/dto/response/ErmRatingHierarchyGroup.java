package ermorg.erm.dto.response;

import lombok.Data;

@Data
public class ErmRatingHierarchyGroup {

	private String key;
	private String displayLabel;
	private long total;
	private ErmHierarchyBreakdown hierarchy = new ErmHierarchyBreakdown();
}
