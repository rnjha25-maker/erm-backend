package com.org_setup_command.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Country extends BaseModel{
    private String name;
    private String code;

    @OneToMany(mappedBy = "country")
    private List<State> states;
}
