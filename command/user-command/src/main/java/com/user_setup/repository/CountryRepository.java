package com.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user_setup.modal.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Country save(Country country);
}
