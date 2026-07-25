package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskTreatment;

@Repository
public interface RiskTreatmentRepository extends JpaRepository<RiskTreatment, Long> {
}
