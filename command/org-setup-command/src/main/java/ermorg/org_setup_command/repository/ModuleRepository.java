package ermorg.org_setup_command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.org_setup_command.modal.Modules;

@Repository
public interface ModuleRepository extends JpaRepository<Modules, Long> {
	
	public Modules getByName(String name);

}
