package ermorg.user_setup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ermorg.user_setup.modal.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Address save(Address address);
}
