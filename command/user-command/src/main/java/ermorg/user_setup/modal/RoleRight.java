package ermorg.user_setup.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="role_right", uniqueConstraints={
		@UniqueConstraint(columnNames={"role_id", "right_id"})
		})
public class RoleRight extends BaseModel{
	
	 
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "right_id")
    private Right right;

//    @Enumerated(EnumType.ORDINAL)
//    private Permission permission;
    
    @Column(name="created_permission")
    private Boolean create;

    @Column(name="update_permission")
    private Boolean update;

    @Column(name="delete_permission")
    private Boolean delete;
    
	@Column(name="view_permission")
	private Boolean view;
	
	@Column(name="export_permission")
	private Boolean export;
	
	@Column(name="print_permission")
	private Boolean print;
	
	@Column(name="approve_permission")
	private Boolean approve;
	
	@Column(name="reject_permission")
	private Boolean reject;
	
	@Column(name="cancel_permission")
	private Boolean cancel;
    
//    @OneToMany//(cascade=CascadeType.MERGE)
//    @JoinColumn(name="right_id")
//    private List<Right> rights;
//    
//    @ManyToOne//(cascade=CascadeType.MERGE)
//    @JoinColumn(name="organization_id")
//    private Organization organization;
//    
//    @ManyToOne
//    @JoinColumn(name="module_id")
//    private Modules module;
}
