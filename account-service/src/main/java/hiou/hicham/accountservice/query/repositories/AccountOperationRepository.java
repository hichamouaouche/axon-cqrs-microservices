package hiou.hicham.accountservice.query.repositories;

import hiou.hicham.accountservice.query.entities.AccountOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acces Spring Data a la projection des operations.
 */
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    List<AccountOperation> findByAccountIdOrderByOperationDateAsc(String accountId);

    AccountOperation findTop1ByAccountIdOrderByOperationDateDesc(String accountId);
}
