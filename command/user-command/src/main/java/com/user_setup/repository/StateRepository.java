package com.user_setup.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.user_setup.modal.State;

@Repository
public interface StateRepository extends CrudRepository<State, Long> {
    State save(State state);
}
