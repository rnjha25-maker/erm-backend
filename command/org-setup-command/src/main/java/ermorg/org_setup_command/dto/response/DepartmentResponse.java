package ermorg.org_setup_command.dto.response;

import ermorg.org_setup_command.modal.Department;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DepartmentResponse {
	
	private long departmentId;
	private String departmentName;
	private long branchId;
	private String branchName;
	private String description;
	
	public DepartmentResponse(Department department) {
		this.departmentId = department.getId();
		this.departmentName = department.getName();
		this.branchId = department.getBranch().getId();
		this.branchName = department.getBranch().getName();
		this.description = department.getDescription();
	}

}
