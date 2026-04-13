package com.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erm.model.history.UserHistory;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

}
