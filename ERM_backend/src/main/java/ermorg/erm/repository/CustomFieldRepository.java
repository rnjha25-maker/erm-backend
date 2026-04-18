package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.CustomField;

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomField, Long> {

}
