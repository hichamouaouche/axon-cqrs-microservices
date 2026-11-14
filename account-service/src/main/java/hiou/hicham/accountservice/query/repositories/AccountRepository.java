package hiou.hicham.accountservice.query.repositories;

import hiou.hicham.accountservice.query.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acces Spring Data a la projection des comptes.
 */
public interface AccountRepository extends JpaRepository<Account, String> {
}
