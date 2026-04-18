package ermorg.erm.util.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.service.FieldService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomResponseMapper {

	private static final ThreadLocal<List<CustomFieldResponse>> customFieldsThread = new ThreadLocal<>();
	@Autowired
	private FieldService fieldService;
	
	public List<CustomResponse> map(String tableName, Long moduleId, Object object, boolean isGrid) throws ResourceNotFoundException {
		
		List<CustomFieldResponse> customFieldResponse  = customFieldsThread.get();
		
		if(customFieldResponse == null) {
			fillFields(moduleId, tableName);
			customFieldResponse  = customFieldsThread.get();
		}
		
		if(isGrid) {
			customFieldResponse = customFieldResponse.stream().filter(customField->customField.getShowGridColumn()).collect(Collectors.toList());
		}
		
		List<CustomResponse> response = new ArrayList<>();

		customFieldResponse.stream()
		.forEach(customField->{
			CustomResponse customResponse;
			try {
				customResponse = CustomResponseMapperUtil.map(object, customField, tableName);
				if(customResponse != null) {
					response.add(customResponse);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("map() {}", e.getMessage());
			}

			
		});
		
		return response;
	}
	
	
	private void fillFields(Long moduleId, String tableName) throws ResourceNotFoundException {
		List<CustomFieldResponse> customFieldResponse = fieldService.getCustomFieldResponse(moduleId, tableName);

		customFieldsThread.set(customFieldResponse);
	}
}
