package com.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.RoleRight;

@Repository
public interface RoleRightRepository extends JpaRepository<RoleRight, Long> {
	

}
