package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.Department;

import lombok.Data;

@Data
public class DepartmentResponse {

	private long organizationId;
	private List<DepartmentDto> departments = new ArrayList<>();
	
	public DepartmentResponse(List<Department> departments, Long organizationId) {
		
		this.organizationId = organizationId;
		this.departments = departments.stream().map(department->new DepartmentDto(department)).collect(Collectors.toList());
	}
}
