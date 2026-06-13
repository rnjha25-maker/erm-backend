package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.util.Date;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RoleType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROLE_TYPE_SEQ")
    @SequenceGenerator(name = "ROLE_TYPE_SEQ", sequenceName = "ROLE_TYPE_SEQ", allocationSize = 1)
    private Integer id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private String name;
    
    private String description;
    
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    public RoleType() {
    }
    
    public RoleType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}
