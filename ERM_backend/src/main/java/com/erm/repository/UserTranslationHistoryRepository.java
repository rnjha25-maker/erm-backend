package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.history.UserTranslationHistory;

@Repository
public interface UserTranslationHistoryRepository extends JpaRepository<UserTranslationHistory, Long> {

}
