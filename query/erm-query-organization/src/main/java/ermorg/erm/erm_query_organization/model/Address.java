package ermorg.erm.erm_query_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Address extends BaseModel {
    private String address;

    @OneToOne
    private Country country;

    @OneToOne
    private State state;

    @OneToOne
    private City city;
}
