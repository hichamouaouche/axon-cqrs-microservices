package hiou.hicham.accountservice.commands;

import hiou.hicham.accountservice.commands.aggregates.AccountAggregate;
import hiou.hicham.accountservice.commands.exceptions.AccountNotActivatedException;
import hiou.hicham.accountservice.commands.exceptions.InsufficientBalanceException;
import hiou.hicham.accountservice.commands.exceptions.NegativeAmountException;
import hiou.hicham.accountservice.commonapi.commands.AddAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.CreditAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.DebitAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.UpdateAccountStatusCommand;
import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import hiou.hicham.accountservice.commonapi.events.AccountActivatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreditedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountDebitedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountStatusUpdatedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests du modele d'ecriture.
 * <p>
 * Le fixture d'Axon suit le schema Given / When / Then propre a l'Event Sourcing :
 * on donne les evenements deja survenus, on envoie une commande, et on verifie les
 * evenements produits (ou l'exception levee) sans aucune base de donnees.
 */
class AccountAggregateTest {

    private static final String ACCOUNT_ID = "ACC-001";

    private FixtureConfiguration<AccountAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(AccountAggregate.class);
    }

    @Test
    @DisplayName("La creation d'un compte produit AccountCreatedEvent puis AccountActivatedEvent")
    void shouldCreateAndActivateAccount() {
        fixture.givenNoPriorActivity()
                .when(new AddAccountCommand(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED)
                );
    }

    @Test
    @DisplayName("Un solde initial negatif est refuse par la fonction de decision")
    void shouldRejectNegativeInitialBalance() {
        fixture.givenNoPriorActivity()
                .when(new AddAccountCommand(ACCOUNT_ID, "Hicham Ouaouche", "MAD", -100))
                .expectException(NegativeAmountException.class)
                .expectNoEvents();
    }

    @Test
    @DisplayName("Le credit d'un compte actif produit AccountCreditedEvent")
    void shouldCreditActivatedAccount() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new CreditAccountCommand(ACCOUNT_ID, "MAD", 8700))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountCreditedEvent(ACCOUNT_ID, "MAD", 8700));
    }

    @Test
    @DisplayName("Le debit d'un compte dont le solde est suffisant produit AccountDebitedEvent")
    void shouldDebitAccountWithSufficientBalance() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED),
                        new AccountCreditedEvent(ACCOUNT_ID, "MAD", 8700))
                .when(new DebitAccountCommand(ACCOUNT_ID, "MAD", 12000))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountDebitedEvent(ACCOUNT_ID, "MAD", 12000));
    }

    @Test
    @DisplayName("Un debit superieur au solde est refuse : l'etat est reconstruit depuis les evenements")
    void shouldRejectDebitWhenBalanceIsInsufficient() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new DebitAccountCommand(ACCOUNT_ID, "MAD", 9000000))
                .expectException(InsufficientBalanceException.class)
                .expectNoEvents();
    }

    @Test
    @DisplayName("Un montant negatif est refuse pour un debit")
    void shouldRejectNegativeDebitAmount() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new DebitAccountCommand(ACCOUNT_ID, "MAD", -50))
                .expectException(NegativeAmountException.class)
                .expectNoEvents();
    }

    @Test
    @DisplayName("Aucune operation n'est autorisee sur un compte suspendu")
    void shouldRejectOperationOnSuspendedAccount() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED),
                        new AccountStatusUpdatedEvent(ACCOUNT_ID, AccountStatus.SUSPENDED))
                .when(new CreditAccountCommand(ACCOUNT_ID, "MAD", 1000))
                .expectException(AccountNotActivatedException.class)
                .expectNoEvents();
    }

    @Test
    @DisplayName("Le changement de statut produit AccountStatusUpdatedEvent")
    void shouldUpdateAccountStatus() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 5400, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new UpdateAccountStatusCommand(ACCOUNT_ID, AccountStatus.SUSPENDED))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountStatusUpdatedEvent(ACCOUNT_ID, AccountStatus.SUSPENDED));
    }

    @Test
    @DisplayName("Le solde de l'agregat est l'agregation de la sequence d'evenements")
    void shouldRebuildBalanceFromEventSequence() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 8000, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED),
                        new AccountDebitedEvent(ACCOUNT_ID, "MAD", 1200),
                        new AccountCreditedEvent(ACCOUNT_ID, "MAD", 6000),
                        new AccountCreditedEvent(ACCOUNT_ID, "MAD", 12000))
                .when(new DebitAccountCommand(ACCOUNT_ID, "MAD", 24800))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountDebitedEvent(ACCOUNT_ID, "MAD", 24800));
    }

    @Test
    @DisplayName("Le solde reconstruit ne permet pas de depasser 24 800 MAD")
    void shouldRejectDebitAboveRebuiltBalance() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, "Hicham Ouaouche", "MAD", 8000, AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED),
                        new AccountDebitedEvent(ACCOUNT_ID, "MAD", 1200),
                        new AccountCreditedEvent(ACCOUNT_ID, "MAD", 6000),
                        new AccountCreditedEvent(ACCOUNT_ID, "MAD", 12000))
                .when(new DebitAccountCommand(ACCOUNT_ID, "MAD", 24801))
                .expectException(InsufficientBalanceException.class);
    }
}
