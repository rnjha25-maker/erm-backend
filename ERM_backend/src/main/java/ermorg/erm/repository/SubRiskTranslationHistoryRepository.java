package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.history.SubRiskTranslationHistory;

@Repository
public interface SubRiskTranslationHistoryRepository extends JpaRepository<SubRiskTranslationHistory, Long> {

}
