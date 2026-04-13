package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.SystemField;

@Repository
public interface SystemFieldRepository extends JpaRepository<SystemField, Long> {

}
