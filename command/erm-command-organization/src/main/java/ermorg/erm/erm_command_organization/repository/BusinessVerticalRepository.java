package ermorg.erm.erm_command_organization.repository;

import java.util.List;
import java.util.Optional;

import ermorg.erm.erm_command_organization.model.BusinessVertical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessVerticalRepository extends JpaRepository<BusinessVertical, Long> {
    List<BusinessVertical> findByCompanyIdAndDeletedFalse(Long companyId);
    
    List<BusinessVertical> findByOrganizationIdAndDeletedFalse(Long organizationId);
    
    List<BusinessVertical> findByCompanyIdAndOrganizationIdAndDeletedFalse(Long companyId, Long organizationId);

    List<BusinessVertical> findByDepartmentIdAndDeletedFalse(Long departmentId);

    List<BusinessVertical> findByBusinessSegment_IdAndDeletedFalse(Long businessSegmentId);
    
    Optional<BusinessVertical> findByIdAndDeletedFalse(Long id);
}
