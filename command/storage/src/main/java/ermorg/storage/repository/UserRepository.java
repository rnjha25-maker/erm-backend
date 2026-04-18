package ermorg.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ermorg.storage.model.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
