package ermorg.erm.erm_command_organization.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResourceWithPermissionsResponse: UI-friendly permission matrix format.
 * 
 * RESPONSE EXAMPLE:
 * {
 *   "resourceId": 1001,
 *   "resourceCode": "RISK_IDENTIFICATION",
 *   "resourceName": "Risk Identification",
 *   "resourceType": "PAGE",
 *   "permissions": {
 *     "VIEW": true,
 *     "CREATE": false,
 *     "EDIT": false,
 *     "DELETE": false
 *   },
 *   "displayOrder": 10
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceWithPermissionsResponse {

    private Long resourceId;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private Integer displayOrder;
    
    /**
     * Permission flags: action -> true/false
     * Standard actions: VIEW, CREATE, EDIT, DELETE, APPROVE, REJECT
     */
    private Map<String, Boolean> permissions;

    /**
     * Convenience method: set a permission flag.
     */
    public void setPermission(String action, boolean allowed) {
        if (permissions == null) {
            permissions = new HashMap<>();
        }
        permissions.put(action, allowed);
    }

    /**
     * Convenience method: check if user has a specific permission.
     */
    public boolean hasPermission(String action) {
        return permissions != null && permissions.getOrDefault(action, false);
    }

    /**
     * Check if user has ANY permission on this resource.
     */
    public boolean hasAnyPermission() {
        return permissions != null && permissions.containsValue(true);
    }
}
