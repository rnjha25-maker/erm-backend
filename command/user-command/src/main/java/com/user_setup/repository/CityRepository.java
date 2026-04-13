package com.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user_setup.modal.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    City save(City city);
}
