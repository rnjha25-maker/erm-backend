package ermorg.erm.util.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.service.IFieldService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomResponseMapper {

	@Autowired
	private IFieldService fieldService;
	
	public List<CustomResponse> map(String tableName, Long moduleId, Object object, boolean isGrid) {

	    List<CustomFieldResponse> customFieldResponse = getFields(moduleId, tableName);

	    if (customFieldResponse == null) {
	        return Collections.emptyList(); // safety check
	    }

	    Stream<CustomFieldResponse> stream = customFieldResponse.stream();

	    if (isGrid) {
	        stream = stream.filter(CustomFieldResponse::getShowGridColumn);
	    }

	    return stream
	            .map(customField -> {
	                try {
	                    return CustomResponseMapperUtil.map(object, customField, tableName);
	                } catch (IllegalArgumentException | IllegalAccessException e) {
	                    log.error("Error mapping field: {}", customField.getFieldName(), e);
	                    return null;
	                }
	            })
	            .filter(Objects::nonNull)
	            .toList();
	}
	
	
	private List<CustomFieldResponse> getFields(Long moduleId, String tableName) {
	    try {
	        return fieldService.getCustomFieldResponse(moduleId, tableName);
	    } catch (ResourceNotFoundException e) {
	        log.error("Error fetching custom fields for moduleId: {}, tableName: {}", moduleId, tableName, e);
	        throw new RuntimeException(e); // convert to unchecked
	    }
	}
}
