package hiou.hicham.accountservice.integration;

import hiou.hicham.accountservice.commonapi.commands.AddAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.CreditAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.DebitAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.UpdateAccountStatusCommand;
import hiou.hicham.accountservice.commonapi.dtos.AccountListDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountResponseDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountStatementDTO;
import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import hiou.hicham.accountservice.commonapi.enums.OperationType;
import hiou.hicham.accountservice.commonapi.events.AccountCreatedEvent;
import hiou.hicham.accountservice.commonapi.queries.GetAccountByIdQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAccountStatementQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAllAccountsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de bout en bout de la chaine CQRS complete :
 * commande -> Command Bus -> agregat -> Event Store -> Event Bus -> projection -> Query Bus.
 * <p>
 * Axon Server n'est pas sollicite : l'Event Store est la base H2 des tests
 * (voir src/test/resources/application.properties).
 */
@SpringBootTest
class AccountFlowIntegrationTest {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private EventStore eventStore;

    @Test
    @DisplayName("Un compte cree est projete dans la base de lecture avec le statut ACTIVATED")
    void shouldCreateAccountAndProjectIt() throws Exception {
        String accountId = createAccount(5400);

        AccountResponseDTO account = queryGateway
                .query(new GetAccountByIdQuery(accountId), ResponseTypes.instanceOf(AccountResponseDTO.class))
                .get();

        assertThat(account.id()).isEqualTo(accountId);
        assertThat(account.customerName()).isEqualTo("Hicham Ouaouche");
        assertThat(account.balance()).isEqualTo(5400);
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVATED);
        assertThat(account.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("Credit puis debit : le solde projete suit la sequence d'evenements")
    void shouldApplyCreditAndDebitOnProjection() throws Exception {
        String accountId = createAccount(5400);

        commandGateway.send(new CreditAccountCommand(accountId, "MAD", 8700)).get();
        commandGateway.send(new DebitAccountCommand(accountId, "MAD", 12000)).get();

        AccountStatementDTO statement = queryGateway
                .query(new GetAccountStatementQuery(accountId), ResponseTypes.instanceOf(AccountStatementDTO.class))
                .get();

        assertThat(statement.account().balance()).isEqualTo(2100);
        assertThat(statement.operations()).hasSize(2);
        assertThat(statement.operations().get(0).type()).isEqualTo(OperationType.CREDIT);
        assertThat(statement.operations().get(0).amount()).isEqualTo(8700);
        assertThat(statement.operations().get(1).type()).isEqualTo(OperationType.DEBIT);
        assertThat(statement.operations().get(1).amount()).isEqualTo(12000);
    }

    @Test
    @DisplayName("Tous les evenements de l'agregat sont conserves dans l'Event Store")
    void shouldStoreEventSequenceInEventStore() throws Exception {
        String accountId = createAccount(8000);
        commandGateway.send(new DebitAccountCommand(accountId, "MAD", 1200)).get();
        commandGateway.send(new CreditAccountCommand(accountId, "MAD", 6000)).get();

        List<String> eventTypes = eventStore.readEvents(accountId)
                .asStream()
                .map(EventMessage::getPayloadType)
                .map(Class::getSimpleName)
                .toList();

        assertThat(eventTypes).containsExactly(
                "AccountCreatedEvent",
                "AccountActivatedEvent",
                "AccountDebitedEvent",
                "AccountCreditedEvent");

        AccountCreatedEvent firstEvent = (AccountCreatedEvent) eventStore.readEvents(accountId)
                .asStream()
                .findFirst()
                .orElseThrow()
                .getPayload();
        assertThat(firstEvent.getBalance()).isEqualTo(8000);
        assertThat(firstEvent.getCurrency()).isEqualTo("MAD");
    }

    @Test
    @DisplayName("Un debit superieur au solde est refuse et le solde projete reste inchange")
    void shouldRejectDebitAndKeepProjectionUnchanged() throws Exception {
        String accountId = createAccount(1000);

        assertThatThrownBy(() -> commandGateway.send(new DebitAccountCommand(accountId, "MAD", 9000000)).get())
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Solde insuffisant");

        AccountResponseDTO account = queryGateway
                .query(new GetAccountByIdQuery(accountId), ResponseTypes.instanceOf(AccountResponseDTO.class))
                .get();
        assertThat(account.balance()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Un compte suspendu refuse les operations")
    void shouldRejectOperationsOnSuspendedAccount() throws Exception {
        String accountId = createAccount(1000);

        commandGateway.send(new UpdateAccountStatusCommand(accountId, AccountStatus.SUSPENDED)).get();

        assertThatThrownBy(() -> commandGateway.send(new CreditAccountCommand(accountId, "MAD", 500)).get())
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("n'est pas actif");

        AccountResponseDTO account = queryGateway
                .query(new GetAccountByIdQuery(accountId), ResponseTypes.instanceOf(AccountResponseDTO.class))
                .get();
        assertThat(account.status()).isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("GetAllAccountsQuery voit tous les comptes crees")
    void shouldListCreatedAccounts() throws Exception {
        String firstAccount = createAccount(100);
        String secondAccount = createAccount(200);

        List<AccountResponseDTO> accounts = queryGateway
                .query(new GetAllAccountsQuery(), ResponseTypes.instanceOf(AccountListDTO.class))
                .get()
                .accounts();

        assertThat(accounts).extracting(AccountResponseDTO::id).contains(firstAccount, secondAccount);
    }

    private String createAccount(double initialBalance) throws Exception {
        String accountId = UUID.randomUUID().toString();
        commandGateway.send(new AddAccountCommand(accountId, "Hicham Ouaouche", "MAD", initialBalance)).get();
        return accountId;
    }
}
