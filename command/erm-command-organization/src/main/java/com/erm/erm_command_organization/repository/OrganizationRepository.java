package com.erm.erm_command_organization.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.erm_command_organization.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization save(Organization organization);

	List<Organization> findAllByCreatedAtBetween(Date startOfWeek, Date endOfWeek);

	Optional<Organization> findByIdAndDeletedFalse(Long id);
}
