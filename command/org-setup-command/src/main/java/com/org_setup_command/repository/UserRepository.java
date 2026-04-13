package com.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);
}
