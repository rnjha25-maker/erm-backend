package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.translation.UserTranslation;

@Repository
public interface UserTranslationRepository extends JpaRepository<UserTranslation, Long> {

}
