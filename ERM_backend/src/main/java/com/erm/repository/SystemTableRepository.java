package com.erm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.model.SystemTable;

@Repository
public interface SystemTableRepository extends JpaRepository<SystemTable, Long> {
	
	@Query("SELECT t FROM SystemTable t WHERE t.module.id = :moduleId")
	public List<SystemTable> findAllByModuleId(@Param("moduleId") Long moduleId);

	public SystemTable findByTableName(String tableName);

}
