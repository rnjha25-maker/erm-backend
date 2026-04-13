package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.history.LanguageHistory;

@Repository
public interface LanguageHistoryRepository extends JpaRepository<LanguageHistory, Long> {

}
