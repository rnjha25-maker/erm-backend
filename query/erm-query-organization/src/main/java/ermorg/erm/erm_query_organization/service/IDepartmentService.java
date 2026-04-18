package ermorg.erm.erm_query_organization.service;

import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Department;

import java.util.List;

public interface IDepartmentService {
    Department getDepartmentById(Long id) throws DataNotFoundException;
    List<Department> getAllDepartments();
}
