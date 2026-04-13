package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.history.RiskHistory;

@Repository
public interface RiskHistoryRepository extends JpaRepository<RiskHistory, Long> {

}
