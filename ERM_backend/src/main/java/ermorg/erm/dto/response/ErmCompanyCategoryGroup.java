package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmCompanyCategoryGroup {

	private String key;
	private String displayLabel;
	private long total;
	private List<NamedCount> byCategory = new ArrayList<>();
}
