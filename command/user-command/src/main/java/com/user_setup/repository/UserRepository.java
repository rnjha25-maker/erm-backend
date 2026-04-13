package com.user_setup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user_setup.modal.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    @Query("SELECT u FROM user u WHERE u.company.id = :companyId AND (u.deleted IS NULL OR u.deleted != true)")
	List<User> findAllUsersByCompanyId(@Param("companyId") Long companyId);

	@Query("SELECT DISTINCT u FROM user u JOIN u.roles r WHERE u.company.id = :companyId "
			+ "AND (u.deleted IS NULL OR u.deleted != true) AND LOWER(r.name) = LOWER(:roleName)")
	List<User> findByCompanyIdAndRoleNameIgnoreCase(@Param("companyId") Long companyId,
			@Param("roleName") String roleName);
}
