package ermorg.erm.erm_command_organization.dto.requestDTO.branchDTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRequest {
	

    private long companyId;
    private String name;
    private String type;
    private long countryId;
    private long stateId;
    private long cityId;
    private String pincode;
    private String address;
    
    private String description;
    
    List<Long> departmentIds;
}
