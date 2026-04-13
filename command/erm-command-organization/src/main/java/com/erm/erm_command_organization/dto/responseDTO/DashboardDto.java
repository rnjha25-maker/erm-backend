package com.erm.erm_command_organization.dto.responseDTO;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class DashboardDto {
	
	private long totalUsers;
	
	
	Map<String, Long> userCounts;
	Map<String, Long> organizationStatus;
	Map<String, Long> planDistribution;
	Map<String, Long> organizationCount;
	Map<String, Long> userGrowth;
	
	public DashboardDto(long totalUsers, Map<String, Long> userGroupCount, Map<String, Long> orgGroupByStatusCount,
			Map<String, Long> orgGroupByPlanCount, Map<String, Long> orgGroupByCityCount,
			Map<String, Long> orgGroupByMonthCount) {

		this.totalUsers =  totalUsers;
		this.userCounts = userGroupCount;
		this.organizationStatus = orgGroupByStatusCount;
		this.planDistribution = orgGroupByPlanCount;
		this.organizationCount = orgGroupByCityCount;
		this.userGrowth = orgGroupByMonthCount;
	}

	
	@Data
	@AllArgsConstructor
	class UserCounts{
		private long tota;
		private long basic;
		private long advanced;
		private long enterprise;
	}
	
	@Data
	@AllArgsConstructor
	class OrganizationStatus{
		private long active;
		private long inactive;
		private long pending;
	}
	
	@Data
	@AllArgsConstructor
	class PlanDistribution{
		private long basic;
		private long advanced;
		private long enterprise;
	}
	
	class OrganizationCount{
		
	}
}
