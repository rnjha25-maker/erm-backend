package com.erm.erm_command_organization.dto.requestDTO.organizationDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private Long id;
    private String name;
    private String orgImgUrl;
}
