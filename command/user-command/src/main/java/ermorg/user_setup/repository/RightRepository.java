package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Right;

@Repository
public interface RightRepository extends JpaRepository<Right, Long> {
	
	

}
