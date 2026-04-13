package com.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.RoleRight;

@Repository
public interface RoleRightMappingRepository extends JpaRepository<RoleRight, Long> {
	
	public List<RoleRight> findByRoleId(Long roleId);

}
