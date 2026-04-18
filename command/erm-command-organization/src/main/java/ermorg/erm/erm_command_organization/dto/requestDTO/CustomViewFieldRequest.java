package ermorg.erm.erm_command_organization.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomViewFieldRequest {
	
	private long fieldId;
	@NotBlank(message="View Field Name must be provided.")
	private String viewFieldName;
	@NotNull(message="Please select Mapped With Column.")
	private long mappedWithColumnId;

}
