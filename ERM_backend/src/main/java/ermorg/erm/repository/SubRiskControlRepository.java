package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.RiskSubControl;

@Repository
public interface SubRiskControlRepository extends JpaRepository<RiskSubControl, Long> {

}
