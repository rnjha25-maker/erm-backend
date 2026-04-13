package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.history.SubRiskHistory;

@Repository
public interface SubRiskHistoryRepository extends JpaRepository<SubRiskHistory, Long> {

}
