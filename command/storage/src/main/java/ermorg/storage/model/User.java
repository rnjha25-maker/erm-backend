package ermorg.storage.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "user")
public class User extends ParentBaseModel{
    private String email;

}
