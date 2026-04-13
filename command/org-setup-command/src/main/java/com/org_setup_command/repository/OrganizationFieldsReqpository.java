package com.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.CustomField;

@Repository
public interface OrganizationFieldsReqpository extends JpaRepository<CustomField, Long> {

}
