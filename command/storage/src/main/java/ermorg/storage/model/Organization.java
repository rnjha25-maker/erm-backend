package ermorg.storage.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Organization extends ParentBaseModel {
    private String name;
    private String organizationLogoImageUrl;

//    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Company> companies;
    
    
//    @OneToMany(mappedBy="organization")
//    private List<Fields> fields;
}
