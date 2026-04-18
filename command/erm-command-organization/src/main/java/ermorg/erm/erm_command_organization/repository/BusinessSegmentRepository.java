package ermorg.erm.erm_command_organization.repository;

import java.util.List;
import java.util.Optional;

import ermorg.erm.erm_command_organization.model.BusinessSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessSegmentRepository extends JpaRepository<BusinessSegment, Long> {
    List<BusinessSegment> findByCompanyIdAndDeletedFalse(Long companyId);
    
    List<BusinessSegment> findByOrganizationIdAndDeletedFalse(Long organizationId);
    
    List<BusinessSegment> findByCompanyIdAndOrganizationIdAndDeletedFalse(Long companyId, Long organizationId);

    List<BusinessSegment> findByDepartmentIdAndDeletedFalse(Long departmentId);
    
    Optional<BusinessSegment> findByIdAndDeletedFalse(Long id);
}
