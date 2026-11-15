package hiou.hicham.accountservice.query.services;

import hiou.hicham.accountservice.commonapi.dtos.AccountResponseDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountStatementDTO;
import hiou.hicham.accountservice.commonapi.queries.GetAccountByIdQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAccountStatementQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAllAccountsQuery;
import hiou.hicham.accountservice.query.entities.Account;
import hiou.hicham.accountservice.query.entities.AccountOperation;
import hiou.hicham.accountservice.query.mappers.AccountMapper;
import hiou.hicham.accountservice.query.repositories.AccountOperationRepository;
import hiou.hicham.accountservice.query.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cote lecture (read side) : repond aux requetes deposees sur le Query Bus.
 * <p>
 * Chaque {@code @QueryHandler} est enregistre aupres du Query Bus avec le type de requete
 * qu'il traite et le type de reponse qu'il produit. Les handlers lisent uniquement la
 * projection relationnelle, jamais l'Event Store.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccountQueryHandler {

    private final AccountRepository accountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final AccountMapper accountMapper;

    @QueryHandler
    public List<AccountResponseDTO> on(GetAllAccountsQuery query) {
        log.info("GetAllAccountsQuery recue");
        return accountMapper.toAccountDTOList(accountRepository.findAll());
    }

    @QueryHandler
    public AccountResponseDTO on(GetAccountByIdQuery query) {
        log.info("GetAccountByIdQuery recue pour le compte {}", query.accountId());
        return accountMapper.toAccountDTO(findAccount(query.accountId()));
    }

    @QueryHandler
    public AccountStatementDTO on(GetAccountStatementQuery query) {
        log.info("GetAccountStatementQuery recue pour le compte {}", query.accountId());
        Account account = findAccount(query.accountId());
        List<AccountOperation> operations =
                accountOperationRepository.findByAccountIdOrderByOperationDateAsc(query.accountId());
        return new AccountStatementDTO(
                accountMapper.toAccountDTO(account),
                accountMapper.toOperationDTOList(operations)
        );
    }

    private Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Compte introuvable : " + accountId));
    }
}
