package ermorg.erm.erm_command_organization.dto.requestDTO.branchDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private Long id;
    private String name;
    private String description;
    private String type;
    private Long addressId;
    private Long companyId;
}
