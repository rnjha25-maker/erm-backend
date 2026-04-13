package com.erm.erm_api_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erm.erm_api_gateway.model.OTPStorage;

@Repository
public interface OTPStorageRepository extends JpaRepository<OTPStorage, Long> {

	@Query("SELECT u FROM OTPStorage u WHERE u.email = :email AND u.verified = false ORDER BY u.createdAt DESC LIMIT 1")
	OTPStorage findByEmail(@Param("email") String email);

}
