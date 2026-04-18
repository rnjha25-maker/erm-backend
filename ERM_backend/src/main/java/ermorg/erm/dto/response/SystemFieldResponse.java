package ermorg.erm.dto.response;

import ermorg.erm.model.SystemField;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemFieldResponse {

	private Long id;
	private String field;
	
	public SystemFieldResponse(SystemField systemField) {
		this.id = systemField.getId();
		this.field = systemField.getField();
	}
}
