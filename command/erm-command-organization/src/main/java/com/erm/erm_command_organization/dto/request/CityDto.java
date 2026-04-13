package com.erm.erm_command_organization.dto.request;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CityDto {
	private long id;
    private String clientIp;
    private Date createdAt;
    private Date updatedAt;
    private String name;
    private long createdBy;
    private long updatedBy;
    @JsonProperty("state_id")
    private long stateId;
}
