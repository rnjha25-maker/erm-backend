package ermorg.user_setup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Company;
import ermorg.user_setup.modal.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role save(Role role);
    
//    @Modifying
//    @Query("UPDATE Role r SET r.organization = NULL WHERE r.organization.id = :organizationId")
//    void nullifyOrganizationId(@Param("organizationId") Long organizationId);
//
//    @Query("SELECT r FROM Role r WHERE r.company.id = :companyId")
//	List<Role> findRolesByCompanyId(@Param("companyId") Long companyId);

}
