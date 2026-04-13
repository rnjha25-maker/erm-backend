package com.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user_setup.modal.RoleRight;

@Repository
public interface RoleRightRepository extends JpaRepository<RoleRight, Long> {

}
