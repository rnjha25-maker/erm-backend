package com.erm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erm.dto.response.CustomResponse;
import com.erm.dto.response.ErmMaturityResponse;
import com.erm.dto.riskDTO.ErmMaturityDto;
import com.erm.dto.riskDTO.ErmMaturityRequest;
import com.erm.exception.ResourceNotFoundException;
import com.erm.model.Company;
import com.erm.model.ERMMaturityAssessment;
import com.erm.model.Organization;
import com.erm.repository.ErmMaturityRepository;
import com.erm.util.CompanyContext;
import com.erm.util.OrganizationContext;
import com.erm.util.mapper.CustomResponseMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public class MaturityService implements IErmMaturityService {

	@Autowired
	private ErmMaturityRepository ermMaturityRepository;
	
	@Autowired
	private CustomResponseMapper customResponseMapper;
	
	@Override
	public ErmMaturityResponse save(ErmMaturityRequest request) throws ResourceNotFoundException {
		
		if(request == null) {
			throw new ResourceNotFoundException("Invailed request.");
		}
		
		List<Long> maturityIds = request.getMaturityRequest().stream().map(req-> req.getMaturityId()).collect(Collectors.toList());
		
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();
		List<ERMMaturityAssessment> ermMaturityList = ermMaturityRepository.getAllByOrgAndMaturityIds(organization.getId(), maturityIds);
		
		request.getMaturityRequest().forEach(req-> {
			ermMaturityList.stream()
				.filter(m -> Objects.equals(m.getId(), req.getMaturityId()))
				.findFirst()
				.ifPresentOrElse(m -> {
					// ✅ if present
					fillFields(m, req, organization, company);
				}, () -> {
					ERMMaturityAssessment m = new ERMMaturityAssessment();
					fillFields(m, req, organization, company);
					
					ermMaturityList.add(m);
				});
		});
		
//		
		
		List<ERMMaturityAssessment> saved = ermMaturityRepository.saveAll(ermMaturityList);
		
//		return new ErmMaturityResponse(saved);
		return null;
	}
	
	private void fillFields(ERMMaturityAssessment m , ErmMaturityDto req, Organization organization, Company company) {
		m.setDeleted(false);
        m.setKeyAssessmentParameters(req.getKeyAssessmentParameters());
        m.setAssessedBy(req.getAssessedBy());
        m.setAssessmentAreaId(req.getAssessmentArea());
        m.setDueDate(req.getDueDate());
        m.setLastAssessmentDate(req.getLastAssessmentDate());
        m.setMarksAchieved(req.getMarksAchieved());
        m.setNextAssessmentDate(req.getNextAssessmentDate());
        m.setOverallMaturityLevel(req.getOverallMaturityLevel());
        m.setStatus(req.getStatus());
        m.setWeightageScore(BigDecimal.valueOf(Double.parseDouble(req.getWeightageScore())));
        m.setOrganization(organization);
        m.setCompany(company);
	}

	@Override
	public ErmMaturityResponse get(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);
		
		if(ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		return new ErmMaturityResponse(ermMaturity);
	}
	
	@Override
	public List<CustomResponse> getView(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);
		
		if(ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		List<CustomResponse> customResponse = customResponseMapper.map("ermMaturity", 1l, new ErmMaturityResponse(ermMaturity), false);
		
		return customResponse;
	}
	
	@Override
	public List<List<CustomResponse>> getAll(Pageable pageable) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Page<ERMMaturityAssessment> ermMaturityList = ermMaturityRepository.getAllByOrg(organization.getId(),pageable);
		
		if(ermMaturityList == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		List<List<CustomResponse>> responseList = new ArrayList<>();
		
		for(ERMMaturityAssessment ermMaturity : ermMaturityList) {
			List<CustomResponse> customResponse = customResponseMapper.map("ermMaturity", 1l, new ErmMaturityResponse(ermMaturity), true);
			responseList.add(customResponse);
		}
		
		
		return responseList;
	}
	
	@Override
	public void delete(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);
		
		if(ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}
		
		ermMaturity.setDeleted(true);
		ermMaturityRepository.save(ermMaturity);
	}
	
	

}
