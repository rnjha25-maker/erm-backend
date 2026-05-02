package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ErmHierarchyBreakdown {
	private List<NamedCount> byCompany = new ArrayList<>();
	private List<NamedCount> byFunction = new ArrayList<>();
	private List<NamedCount> byBranch = new ArrayList<>();
}
