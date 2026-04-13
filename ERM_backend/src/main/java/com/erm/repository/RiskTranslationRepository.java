package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.translation.RiskTranslation;

@Repository
public interface RiskTranslationRepository extends JpaRepository<RiskTranslation, Long> {

}
