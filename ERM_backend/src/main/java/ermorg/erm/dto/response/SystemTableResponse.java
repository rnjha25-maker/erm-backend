package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.model.SystemTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemTableResponse {

	private Long id;
	private String tableName;
	private List<SystemFieldResponse> fields = new ArrayList<>();
	
	public SystemTableResponse(SystemTable systemTable) {
		this.id = systemTable.getId();
		this.tableName = systemTable.getTableName();
		if (systemTable.getFields() != null) {
			this.fields = systemTable.getFields().stream()
					.filter(field -> !field.getDeleted())
					.map(field -> new SystemFieldResponse(field))
					.collect(Collectors.toList());
		}
	}
}
