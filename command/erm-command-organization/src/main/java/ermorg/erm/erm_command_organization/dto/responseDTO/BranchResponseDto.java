package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.Branch;

import lombok.Data;

@Data
public class BranchResponseDto {
	
	private long branchId;
	private String branchName;
    private String type;
    private long countryId;
    private long stateId;
    private long cityId;
    private String pincode;
    private String address;
    private String description;
    private List<DepartmentDto> departments = new ArrayList<>();
    
    public BranchResponseDto(Branch branch) {
		this.branchId = branch.getId();
		this.branchName = branch.getName();
		this.type = branch.getType();
		this.countryId = branch.getCountry().getId();
		this.stateId = branch.getState().getId();
		this.cityId = branch.getCity().getId();
		this.pincode = branch.getPincode();
		this.address = branch.getAddress();
		this.description = branch.getDescription();
		this.departments = branch.getDepartments().stream().map(department->new DepartmentDto(department)).collect(Collectors.toList());
	}

}
