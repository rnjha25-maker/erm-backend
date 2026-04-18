package ermorg.org_setup_command.modal;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BASE_MODEL_SEQ")
    @SequenceGenerator(name = "BASE_MODEL_SEQ", sequenceName = "BASE_MODEL_SEQ", allocationSize = 1)
    private Long id;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createdAt;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    protected Date updatedAt;

    @ManyToOne
    @CreatedBy
    protected User createdBy;

    @ManyToOne
    @LastModifiedBy
    protected User updatedBy;

    @Column(name = "client_ip")
    protected String  clientIP;
    
    private Boolean deleted = false;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseModel other = (BaseModel) obj;
        return Objects.equals(id, other.id);
    }
    
    @PrePersist
    public void onPrePersist() {
    	
    	this.clientIP = createdBy != null ? createdBy.getIp() : null;
        this.updatedBy = null;  
        this.updatedAt = null;
    }

    @PreUpdate
    public void onPreUpdate() {
       
    	this.clientIP = updatedBy != null ? updatedBy.getIp() : null;

        if (this.updatedBy == null && this.createdBy != null) {
            this.updatedBy = this.createdBy;  
        }
        if (this.updatedAt == null && this.createdAt != null) {
            this.updatedAt = this.createdAt;  
        }
    }
    
}
