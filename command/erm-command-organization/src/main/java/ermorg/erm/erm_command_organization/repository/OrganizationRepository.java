package ermorg.erm.erm_command_organization.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_command_organization.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    // ✅ Removed: save() — already inherited from JpaRepository, no need to redeclare

    List<Organization> findAllByCreatedAtBetween(Date startOfWeek, Date endOfWeek);

    Optional<Organization> findByIdAndDeletedFalse(Long id);

    List<Organization> findAllByDeletedFalse(); // ✅ Added: used in getAllOrganization()
}