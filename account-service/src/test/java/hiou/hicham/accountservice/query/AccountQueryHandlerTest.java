package hiou.hicham.accountservice.query;

import hiou.hicham.accountservice.commonapi.dtos.AccountStatementDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountWatchEventDTO;
import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import hiou.hicham.accountservice.commonapi.enums.OperationType;
import hiou.hicham.accountservice.commonapi.queries.GetAccountByIdQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAccountStatementQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAllAccountsQuery;
import hiou.hicham.accountservice.commonapi.queries.WatchAccountQuery;
import hiou.hicham.accountservice.query.entities.Account;
import hiou.hicham.accountservice.query.entities.AccountOperation;
import hiou.hicham.accountservice.query.mappers.AccountMapper;
import hiou.hicham.accountservice.query.repositories.AccountOperationRepository;
import hiou.hicham.accountservice.query.repositories.AccountRepository;
import hiou.hicham.accountservice.query.services.AccountNotFoundException;
import hiou.hicham.accountservice.query.services.AccountQueryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests des Query Handlers sur une base de lecture reelle (H2).
 */
@DataJpaTest
class AccountQueryHandlerTest {

    private static final String ACCOUNT_ID = "ACC-003";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountOperationRepository accountOperationRepository;

    private AccountQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        queryHandler = new AccountQueryHandler(accountRepository, accountOperationRepository, new AccountMapper());

        Account account = accountRepository.save(Account.builder()
                .id(ACCOUNT_ID)
                .customerName("Hicham Ouaouche")
                .currency("MAD")
                .balance(2100)
                .status(AccountStatus.ACTIVATED)
                .createdAt(Instant.parse("2026-11-11T09:00:00Z"))
                .build());

        accountOperationRepository.save(AccountOperation.builder()
                .account(account)
                .amount(8700)
                .type(OperationType.CREDIT)
                .operationDate(Instant.parse("2026-11-11T09:05:00Z"))
                .build());
        accountOperationRepository.save(AccountOperation.builder()
                .account(account)
                .amount(12000)
                .type(OperationType.DEBIT)
                .operationDate(Instant.parse("2026-11-11T09:10:00Z"))
                .build());
    }

    @Test
    @DisplayName("GetAllAccountsQuery renvoie les comptes projetes")
    void shouldReturnAllAccounts() {
        assertThat(queryHandler.on(new GetAllAccountsQuery()).accounts())
                .hasSize(1)
                .allSatisfy(dto -> {
                    assertThat(dto.id()).isEqualTo(ACCOUNT_ID);
                    assertThat(dto.balance()).isEqualTo(2100);
                });
    }

    @Test
    @DisplayName("GetAccountByIdQuery renvoie le compte demande")
    void shouldReturnAccountById() {
        assertThat(queryHandler.on(new GetAccountByIdQuery(ACCOUNT_ID)).customerName())
                .isEqualTo("Hicham Ouaouche");
    }

    @Test
    @DisplayName("GetAccountByIdQuery echoue sur un identifiant inconnu")
    void shouldFailOnUnknownAccount() {
        assertThatThrownBy(() -> queryHandler.on(new GetAccountByIdQuery("inconnu")))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("GetAccountStatementQuery renvoie le releve classe par date")
    void shouldReturnAccountStatement() {
        AccountStatementDTO statement = queryHandler.on(new GetAccountStatementQuery(ACCOUNT_ID));

        assertThat(statement.account().id()).isEqualTo(ACCOUNT_ID);
        assertThat(statement.operations()).hasSize(2);
        assertThat(statement.operations().get(0).type()).isEqualTo(OperationType.CREDIT);
        assertThat(statement.operations().get(1).type()).isEqualTo(OperationType.DEBIT);
    }

    @Test
    @DisplayName("WatchAccountQuery renvoie la derniere operation connue comme resultat initial")
    void shouldReturnLastOperationAsInitialResult() {
        AccountWatchEventDTO watchEvent = queryHandler.on(new WatchAccountQuery(ACCOUNT_ID));

        assertThat(watchEvent.currentBalance()).isEqualTo(2100);
        assertThat(watchEvent.type()).isEqualTo(OperationType.DEBIT);
        assertThat(watchEvent.operationAmount()).isEqualTo(12000);
    }
}
