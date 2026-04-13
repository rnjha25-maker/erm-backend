package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.SubRisk;

@Repository
public interface SubRiskRepository extends JpaRepository<SubRisk, Long> {

}
