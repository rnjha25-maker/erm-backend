package com.org_setup_command.dto.response;

import com.org_setup_command.modal.Branch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchResponse {

	private long branchId;
	private String branchName;
	private long countryId;
	private String countryName;
	private long stateId;
	private String stateName;
	private long cityId;
	private String cityName;
	private String address;

	public BranchResponse(Branch branch) {

		this.branchId = branch.getId();
		this.branchName = branch.getName();
		this.countryId =  branch.getCountry() != null ? branch.getCountry().getId() : 0;
		this.countryName = branch.getCountry() != null ? branch.getCountry().getName() : "";
		this.stateId = branch.getState() != null ? branch.getState().getId() : 0;
		this.stateName = branch.getState() != null ? branch.getState().getName() : "";
		this.cityId = branch.getCity() != null ? branch.getCity().getId() : 0;
		this.cityName = branch.getCity() != null ? branch.getCity().getName() : "";
		this.address = branch.getAddress();

	}
}
