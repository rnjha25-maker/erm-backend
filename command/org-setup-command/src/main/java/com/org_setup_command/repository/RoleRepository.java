package com.org_setup_command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role save(Role role);
    
    @Modifying
    @Query("UPDATE Role r SET r.organization = NULL WHERE r.organization.id = :organizationId")
    void nullifyOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT r FROM Role r where r.company.id = :companyId AND (r.deleted IS NULL OR r.deleted != true)")
    public List<Role> getRolesByCompanyId(@Param("companyId") Long company);
    
    @Query("SELECT r FROM Role r where r.organization.id = :organizationId AND (r.deleted IS NULL OR r.deleted != true)")
    public List<Role> getRolesByOrganizationId(@Param("organizationId") Long company);

}
