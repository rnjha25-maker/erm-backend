package ermorg.erm.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.response.EscalationResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.response.RiskAssessmentResponse;
import ermorg.erm.dto.response.RiskControlResponse;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.dto.response.RiskSubControlResponse;
import ermorg.erm.dto.response.SubRiskResponse;
import ermorg.erm.model.Modules;
import ermorg.erm.model.SystemField;
import ermorg.erm.model.SystemTable;
import ermorg.erm.repository.ModuleRepository;
import ermorg.erm.repository.SystemTableRepository;

@RestController
@RequestMapping("/setup")
public class SetupController {
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private SystemTableRepository systemTableRepository;
	
	@GetMapping("/tables")
	private void insertSystemTables() {
		 List<Modules> modules = moduleRepository.findAll();
		 
		 List<SystemTable> systemTables = new ArrayList<>();
		 
		 SystemTable riskTable = getTableObj("risk", 1, modules);
		 populateSystemFields(riskTable, RiskResponse.class);
		 systemTables.add(riskTable);
		 
		 SystemTable ermMaturityTable = getTableObj("ermMaturity", 1, modules);
		 populateSystemFields(ermMaturityTable, ErmMaturityResponse.class);
		 systemTables.add(ermMaturityTable);
		 
		 SystemTable escalationTable = getTableObj("escalation", 1, modules);
		 populateSystemFields(escalationTable, EscalationResponse.class);
		 systemTables.add(escalationTable);
		 
		 SystemTable kriKpiTable = getTableObj("kriKpiReview", 1, modules);
		 populateSystemFields(kriKpiTable, KriKpiReviewResponseDTO.class);
		 systemTables.add(kriKpiTable);
		 
		 SystemTable riskAssessmentTable = getTableObj("riskAssessment", 1, modules);
		 populateSystemFields(riskAssessmentTable, RiskAssessmentResponse.class);
		 systemTables.add(riskAssessmentTable);
		 
		 SystemTable riskControlTable = getTableObj("riskcontrol", 1, modules);
		 populateSystemFields(riskControlTable, RiskControlResponse.class);
		 systemTables.add(riskControlTable);
		 
		 SystemTable riskResponseTreatmentTable = getTableObj("riskTreatment", 1, modules);
		 populateSystemFields(riskResponseTreatmentTable, RiskResponseTreatmentResponse.class);
		 systemTables.add(riskResponseTreatmentTable);
		 
		 SystemTable riskReviewResponseDtoTable = getTableObj("riskReview", 1, modules);
		 populateSystemFields(riskReviewResponseDtoTable, RiskReviewResponseDtoResponse.class);
		 systemTables.add(riskReviewResponseDtoTable);
		 
		 SystemTable riskSubControlTable = getTableObj("riskSubControl", 1, modules);
		 populateSystemFields(riskSubControlTable, RiskSubControlResponse.class);
		 systemTables.add(riskSubControlTable);
		 
		 SystemTable subRiskTable = getTableObj("subRisk", 1, modules);
		 populateSystemFields(subRiskTable, SubRiskResponse.class);
		 systemTables.add(subRiskTable);
		 
		 systemTableRepository.saveAll(systemTables);
		 
	 }
	 
	 
	 
	private SystemTable getTableObj(String tableName, long moduleId, List<Modules> modules) {
		 SystemTable  systemTable = new SystemTable();
		 
		Modules module = modules.stream().filter(m -> m.getId() == moduleId).findFirst()
		.orElseThrow();
		 
		 systemTable.setTableName(tableName);
		 systemTable.setModule(module);
		 systemTable.setDeleted(false);
		 
		 return systemTable;
	 }
	 
	 
	 
	 private void populateSystemFields(SystemTable systemTable, Class classObj) {
		 
		  List<SystemField> systemFields = new ArrayList<>();
		 Field[] fields = classObj.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				String name = field.getName();
				SystemField systemField = new SystemField();
				systemField.setDeleted(false);
				systemField.setSystemTable(systemTable);
				systemField.setField(name);
				
				systemFields.add(systemField);
				
			}
			
			systemTable.setFields(systemFields);
	 }

}
