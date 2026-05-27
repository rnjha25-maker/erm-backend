package ermorg.erm.erm_api_gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ermorg.erm.erm_api_gateway.model.User;
import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//	@Query("SELECT u FROM User u WHERE u.email = :email")
//	List<User> getUserByEmail(@Param("email") String email); 

	@Query("""
		    SELECT DISTINCT u
		    FROM user u
		    LEFT JOIN FETCH u.roles r
		    LEFT JOIN FETCH r.roleRights rr
		    WHERE u.email = :email
		""")
		Optional<User> findByEmailWithRoles(@Param("email") String email);

}
