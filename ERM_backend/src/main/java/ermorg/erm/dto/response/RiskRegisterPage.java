package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class RiskRegisterPage {

	private List<RiskRegisterRow> content = new ArrayList<>();
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;

	public static RiskRegisterPage of(List<RiskRegisterRow> content, int page, int size, long totalElements) {
		RiskRegisterPage result = new RiskRegisterPage();
		result.setContent(content);
		result.setPage(page);
		result.setSize(size);
		result.setTotalElements(totalElements);
		result.setTotalPages(size == 0 ? 0 : (int) Math.ceil((double) totalElements / size));
		return result;
	}
}
