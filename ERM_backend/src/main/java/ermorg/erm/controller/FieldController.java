package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.SystemFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IFieldService;

@RestController
@RequestMapping("/field")
public class FieldController {

    @Autowired
    private IFieldService fieldService;

    @GetMapping("/get-system-tables/{moduleId:[\\d]+}")
    public GeneralResponse<List<SystemTableResponse>> getSystemTables(
            @PathVariable Long moduleId) throws ResourceNotFoundException {

        GeneralResponse<List<SystemTableResponse>> response = new GeneralResponse<>();
        response.setData(fieldService.getSystemTables(moduleId));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("System tables fetched");
        return response;
    }

    @GetMapping("/get-system-table-by-name/{tableName}")
    public GeneralResponse<SystemTableResponse> getSystemTableByName(
            @PathVariable String tableName) throws ResourceNotFoundException {

        GeneralResponse<SystemTableResponse> response = new GeneralResponse<>();
        response.setData(fieldService.getSystemTableByName(tableName));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("System table fetched");
        return response;
    }

    @GetMapping("/get-all-category-fields/{id:[\\d]+}")
    public GeneralResponse<List<CategoryListResponse>> getAllCategories(
            @PathVariable("id") Long moduleId) throws ResourceNotFoundException {

        GeneralResponse<List<CategoryListResponse>> response = new GeneralResponse<>();
        response.setData(fieldService.getAllCategories(moduleId));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Categories fetched");
        return response;
    }

    @GetMapping("/get-category/{id:[\\d]+}")
    public GeneralResponse<CategoryResponse> getCategory(
            @PathVariable("id") Long categoryId) throws ResourceNotFoundException {

        GeneralResponse<CategoryResponse> response = new GeneralResponse<>();
        response.setData(fieldService.getCategory(categoryId));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Category fetched");
        return response;
    }

    /**
     * Returns active dropdown options for a system field.
     *
     * GET /field/options/{tableName}/{fieldName}
     *
     * Example: GET /field/options/riskAssessment/valueUnit
     * Returns: [{"value":"RS","label":"Rs.","displayOrder":1}, ...]
     *
     * This endpoint enables fully metadata-driven dropdown rendering.
     * Adding a new option = one DB INSERT, zero code changes.
     */
    @GetMapping("/options/{tableName}/{fieldName}")
    public GeneralResponse<List<SystemFieldResponse.FieldOptionResponse>> getFieldOptions(
            @PathVariable String tableName,
            @PathVariable String fieldName) throws ResourceNotFoundException {

        GeneralResponse<List<SystemFieldResponse.FieldOptionResponse>> response = new GeneralResponse<>();
        response.setData(fieldService.getFieldOptions(fieldName, tableName));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Field options fetched");
        return response;
    }
}
