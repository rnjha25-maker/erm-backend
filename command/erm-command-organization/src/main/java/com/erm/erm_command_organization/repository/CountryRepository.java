package com.erm.erm_command_organization.repository;

import com.erm.erm_command_organization.dto.responseDTO.CountryResponse;
import com.erm.erm_command_organization.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Country save(Country country);
}
