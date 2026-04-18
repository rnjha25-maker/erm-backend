package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.UserDetail;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    UserDetail save(UserDetail userDetail);
    
    @Modifying
    @Query("DELETE FROM UserDetail u WHERE u.organization.id = :organizationId")
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Modifying
    @Query("UPDATE UserDetail u SET u.organization = NULL WHERE u.organization.id = :organizationId")
    void nullifyOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT COUNT(u) FROM UserDetail u WHERE u.email = :email")
    public Long countByEmail(String email);
}
