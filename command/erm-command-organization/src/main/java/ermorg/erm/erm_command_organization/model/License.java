package ermorg.erm.erm_command_organization.model;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ermorg.erm.erm_command_organization.enums.LicenseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "license")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "plan_type")
    private String planType;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer gracePeriodDays;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    private Boolean autoRenew;

    private LocalDateTime lastRenewedAt;
    @Column(name="created_at")
    private LocalDate createdDate;
    
    @Column(name = "updated_at")
    private LocalDate updateDate;
    
    
}