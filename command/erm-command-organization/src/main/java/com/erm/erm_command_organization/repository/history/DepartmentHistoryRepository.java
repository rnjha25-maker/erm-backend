package com.erm.erm_command_organization.repository.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.erm_command_organization.model.history.DepartmentHistory;

@Repository
public interface DepartmentHistoryRepository extends JpaRepository<DepartmentHistory, Long> {

}
