package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.history.SubRiskHistory;

@Repository
public interface SubRiskHistoryRepository extends JpaRepository<SubRiskHistory, Long> {

}
