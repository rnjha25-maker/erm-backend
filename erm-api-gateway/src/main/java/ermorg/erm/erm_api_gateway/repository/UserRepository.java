package ermorg.erm.erm_api_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_api_gateway.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//	@Query("SELECT u FROM User u WHERE u.email = :email")
//	List<User> getUserByEmail(@Param("email") String email); 

	User findUserByEmail(String email);

}
