package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.translation.Language;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {

	public Language getLanguageByLanguageCode(String languageCode);

}
