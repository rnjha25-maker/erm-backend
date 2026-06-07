package ermorg.erm.erm_command_organization.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.PermissionAction;
import ermorg.erm.erm_command_organization.model.RolePermission;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * Get all permissions for a specific role (active only).
     */
    List<RolePermission> findByRoleIdAndDeletedFalse(Long roleId);

    /**
     * Get all permissions by role code.
     */
    @Query("""
            select rp from RolePermission rp
            join rp.role r
            join rp.resource res
            where r.roleCode = :roleCode
              and r.deleted = false
              and res.deleted = false
              and rp.deleted = false
            """)
    List<RolePermission> findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * Get all allowed permissions for a user (action + resource level).
     * Includes status checks: user active, role active, resource active.
     */
    @Query("""
            select rp from User u
            join u.roles r
            join RolePermission rp on rp.role.id = r.id
            join rp.resource res
            where u.id = :userId
              and u.deleted = false
              and r.deleted = false
              and (r.active is null or r.active = true)
              and res.deleted = false
              and res.active = true
              and rp.deleted = false
              and rp.allowed = true
            """)
    List<RolePermission> findAllowedByUserId(@Param("userId") Long userId);

    /**
     * Get user's permissions filtered by resource code.
     */
    @Query("""
            select rp from User u
            join u.roles r
            join RolePermission rp on rp.role.id = r.id
            join rp.resource res
            where u.id = :userId
              and u.deleted = false
              and r.deleted = false
              and (r.active is null or r.active = true)
              and res.resourceCode = UPPER(:resourceCode)
              and res.deleted = false
              and res.active = true
              and rp.deleted = false
              and rp.allowed = true
            """)
    List<RolePermission> findAllowedByUserIdAndResourceCode(
            @Param("userId") Long userId,
            @Param("resourceCode") String resourceCode);

    /**
     * Get user's permissions filtered by resource type (MODULE, PAGE, FEATURE, etc).
     */
    @Query("""
            select rp from User u
            join u.roles r
            join RolePermission rp on rp.role.id = r.id
            join rp.resource res
            where u.id = :userId
              and u.deleted = false
              and r.deleted = false
              and (r.active is null or r.active = true)
              and res.resourceType = UPPER(:resourceType)
              and res.deleted = false
              and res.active = true
              and rp.deleted = false
              and rp.allowed = true
            """)
    List<RolePermission> findAllowedByUserIdAndResourceType(
            @Param("userId") Long userId,
            @Param("resourceType") String resourceType);

    /**
     * Core permission check: Does user have action on resource?
     * Status checks: user active, role active, resource active, permission allowed.
     */
    @Query("""
            select count(rp) > 0 from User u
            join u.roles r
            join RolePermission rp on rp.role.id = r.id
            join rp.resource res
            where u.id = :userId
              and u.deleted = false
              and r.deleted = false
              and (r.active is null or r.active = true)
              and res.resourceCode = :resourceCode
              and res.deleted = false
              and res.active = true
              and rp.action = :action
              and rp.deleted = false
              and rp.allowed = true
            """)
    boolean existsAllowedPermission(@Param("userId") Long userId,
            @Param("resourceCode") String resourceCode,
            @Param("action") PermissionAction action);

    /**
     * Count active permissions for a role.
     * Used for validation and role-status analysis.
     */
    @Query("""
            select count(rp) from RolePermission rp
            where rp.role.id = :roleId
              and rp.allowed = true
              and rp.deleted = false
            """)
    long countActivePermissionsForRole(@Param("roleId") Long roleId);
}
