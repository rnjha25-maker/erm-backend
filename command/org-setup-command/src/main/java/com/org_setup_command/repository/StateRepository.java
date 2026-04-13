package com.org_setup_command.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.org_setup_command.modal.State;

@Repository
public interface StateRepository extends CrudRepository<State, Long> {
    State save(State state);
}
