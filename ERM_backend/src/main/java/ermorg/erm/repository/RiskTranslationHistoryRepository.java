package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.history.RiskTranslationHistory;

@Repository
public interface RiskTranslationHistoryRepository extends JpaRepository<RiskTranslationHistory, Long> {

}
