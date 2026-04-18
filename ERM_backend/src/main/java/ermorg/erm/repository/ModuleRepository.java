package ermorg.erm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.model.Modules;


@Repository
public interface ModuleRepository extends JpaRepository<Modules, Long> {

}
