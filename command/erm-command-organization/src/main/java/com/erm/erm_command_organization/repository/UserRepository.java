package com.erm.erm_command_organization.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.erm_command_organization.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

	List<User> findAllByCreatedAtBetween(Date startDate, Date endDate);
}
