package com.org_setup_command.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class City extends BaseModel {
    private String name;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;
}
