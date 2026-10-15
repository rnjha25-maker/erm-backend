package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmGroupBreakdown {

	private String key;
	private String displayLabel;
	private long total;
	private List<NamedCount> breakdown = new ArrayList<>();
}
