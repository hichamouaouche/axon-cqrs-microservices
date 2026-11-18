package hiou.hicham.accountservice.query;

import hiou.hicham.accountservice.commonapi.dtos.AccountWatchEventDTO;
import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import hiou.hicham.accountservice.commonapi.enums.OperationType;
import hiou.hicham.accountservice.commonapi.events.AccountActivatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreditedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountDebitedEvent;
import hiou.hicham.accountservice.commonapi.queries.WatchAccountQuery;
import hiou.hicham.accountservice.query.entities.Account;
import hiou.hicham.accountservice.query.entities.AccountOperation;
import hiou.hicham.accountservice.query.repositories.AccountOperationRepository;
import hiou.hicham.accountservice.query.repositories.AccountRepository;
import hiou.hicham.accountservice.query.services.AccountEventHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de la projection : chaque evenement doit mettre la base de lecture a jour
 * et notifier les clients abonnes au flux temps reel.
 */
@ExtendWith(MockitoExtension.class)
class AccountEventHandlerTest {

    private static final String ACCOUNT_ID = "ACC-002";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountOperationRepository accountOperationRepository;

    @Mock
    private QueryUpdateEmitter queryUpdateEmitter;

    @InjectMocks
    private AccountEventHandler eventHandler;

    @Test
    @DisplayName("AccountCreatedEvent insere le compte dans la base de lecture")
    void shouldProjectAccountCreatedEvent() {
        AccountCreatedEvent event =
                new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED);

        eventHandler.on(event, new GenericEventMessage<>(event));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getCustomerName()).isEqualTo("Hicham Ouaouche");
        assertThat(saved.getBalance()).isEqualTo(5400);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.CREATED);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("AccountActivatedEvent fait passer le compte projete au statut ACTIVATED")
    void shouldProjectAccountActivatedEvent() {
        Account account = account(5400, AccountStatus.CREATED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        eventHandler.on(new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED));

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVATED);
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("AccountCreditedEvent enregistre une operation CREDIT et augmente le solde")
    void shouldProjectAccountCreditedEvent() {
        Account account = account(5400, AccountStatus.ACTIVATED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        AccountCreditedEvent event = new AccountCreditedEvent(ACCOUNT_ID, "MAD", 8700);

        eventHandler.on(event, new GenericEventMessage<>(event));

        ArgumentCaptor<AccountOperation> operationCaptor = ArgumentCaptor.forClass(AccountOperation.class);
        verify(accountOperationRepository).save(operationCaptor.capture());
        assertThat(operationCaptor.getValue().getType()).isEqualTo(OperationType.CREDIT);
        assertThat(operationCaptor.getValue().getAmount()).isEqualTo(8700);
        assertThat(account.getBalance()).isEqualTo(14100);
        verify(queryUpdateEmitter).emit(eq(WatchAccountQuery.class), any(), any(AccountWatchEventDTO.class));
    }

    @Test
    @DisplayName("AccountDebitedEvent enregistre une operation DEBIT et diminue le solde")
    void shouldProjectAccountDebitedEvent() {
        Account account = account(14100, AccountStatus.ACTIVATED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        AccountDebitedEvent event = new AccountDebitedEvent(ACCOUNT_ID, "MAD", 12000);

        eventHandler.on(event, new GenericEventMessage<>(event));

        ArgumentCaptor<AccountOperation> operationCaptor = ArgumentCaptor.forClass(AccountOperation.class);
        verify(accountOperationRepository).save(operationCaptor.capture());
        assertThat(operationCaptor.getValue().getType()).isEqualTo(OperationType.DEBIT);
        assertThat(account.getBalance()).isEqualTo(2100);
        verify(queryUpdateEmitter).emit(eq(WatchAccountQuery.class), any(), any(AccountWatchEventDTO.class));
    }

    private Account account(double balance, AccountStatus status) {
        return Account.builder()
                .id(ACCOUNT_ID)
                .customerName("Hicham Ouaouche")
                .currency("MAD")
                .balance(balance)
                .status(status)
                .build();
    }
}
