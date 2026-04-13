package com.erm.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BasicDashboardResponse {
	Map<String, Long> countByStatus = new HashMap<>();
	
	private List<List<CustomResponse>> topRisks = new ArrayList<>();

	

	public BasicDashboardResponse(Map<String, Long> countByStatus, List<List<CustomResponse>> responseList) {
		this.topRisks = responseList;
		this.countByStatus = countByStatus;
	}
	
	
}
