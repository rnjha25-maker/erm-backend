package com.erm.erm_command_organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.erm_command_organization.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

