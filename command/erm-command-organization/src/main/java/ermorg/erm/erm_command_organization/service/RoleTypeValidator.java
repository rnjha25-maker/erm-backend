package ermorg.erm.erm_command_organization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.model.RoleType;
import ermorg.erm.erm_command_organization.repository.RoleTypeRepository;
import java.util.Optional;

/**
 * Service for validating RoleType operations.
 * Provides methods to check if a RoleType exists and is valid for specific use cases.
 */
@Service
public class RoleTypeValidator {
    
    @Autowired
    private RoleTypeRepository roleTypeRepository;
    
    /**
     * Validates if a roleTypeCode exists in the database.
     * 
     * @param roleTypeCode the code to validate
     * @return Optional containing RoleType if found
     * @throws IllegalArgumentException if roleTypeCode is null or invalid
     */
    public Optional<RoleType> validateRoleType(String roleTypeCode) {
        if (roleTypeCode == null || roleTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Role type code cannot be null or empty.");
        }
        
        Optional<RoleType> roleType = roleTypeRepository.findByCode(roleTypeCode.trim());
        
        if (roleType.isEmpty()) {
            throw new IllegalArgumentException("Invalid role type: " + roleTypeCode);
        }
        
        return roleType;
    }
    
    /**
     * Checks if a roleTypeCode is valid for dashboard operations.
     * Currently, only BUSINESS and CUSTOM types are allowed for dashboard.
     * SYSTEM and ADMIN roles should be created/managed through administrative interfaces.
     * 
     * @param roleTypeCode the role type code to check
     * @return true if valid for dashboard, false otherwise
     */
    public boolean isRoleTypeValidForDashboard(String roleTypeCode) {
        if (roleTypeCode == null || roleTypeCode.trim().isEmpty()) {
            return false;
        }
        
        // Allow only BUSINESS and CUSTOM for dashboard operations
        String trimmedCode = roleTypeCode.trim();
        return trimmedCode.equals("BUSINESS") || trimmedCode.equals("CUSTOM");
    }
    
    /**
     * Checks if a RoleType exists in the database.
     * 
     * @param roleTypeCode the code to check
     * @return true if exists, false otherwise
     */
    public boolean exists(String roleTypeCode) {
        if (roleTypeCode == null || roleTypeCode.trim().isEmpty()) {
            return false;
        }
        
        return roleTypeRepository.findByCode(roleTypeCode.trim()).isPresent();
    }
    
}
