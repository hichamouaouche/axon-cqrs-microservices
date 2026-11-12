package hiou.hicham.accountservice.commands.aggregates;

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
import hiou.hicham.accountservice.commands.exceptions.AccountNotActivatedException;
import hiou.hicham.accountservice.commands.exceptions.InsufficientBalanceException;
import hiou.hicham.accountservice.commands.exceptions.NegativeAmountException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

/**
 * Agregat du compte bancaire : c'est le modele d'ecriture de l'application.
 * <p>
 * Il porte les deux fonctions de l'Event Sourcing :
 * <ul>
 *   <li>la fonction de decision, implementee par les {@code @CommandHandler} :
 *       {@code (State, Command) => List[Event]}. Elle verifie les regles metier et
 *       publie les evenements via {@link AggregateLifecycle#apply(Object)} ;</li>
 *   <li>la fonction d'evolution, implementee par les {@code @EventSourcingHandler} :
 *       {@code (State, Event) => State}. Elle ne contient aucune regle metier,
 *       elle ne fait que muter l'etat de l'agregat.</li>
 * </ul>
 * L'etat courant n'est jamais stocke : Axon le reconstruit en rejouant, a chaque
 * commande, la sequence d'evenements de l'agregat lue depuis l'Event Store.
 */
@Aggregate
@Slf4j
public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;
    private String customerName;
    private String currency;
    private double balance;
    private AccountStatus status;

    /**
     * Constructeur sans argument exige par Axon pour instancier l'agregat avant le replay.
     */
    public AccountAggregate() {
    }

    // ------------------------------------------------------------------
    // Fonction de decision : (State, Command) => List[Event]
    // ------------------------------------------------------------------

    @CommandHandler
    public AccountAggregate(AddAccountCommand command) {
        log.info("AddAccountCommand recue : {}", command);
        if (command.getInitialBalance() < 0) {
            throw new NegativeAmountException("Le solde initial ne peut pas etre negatif");
        }
        AggregateLifecycle.apply(new AccountCreatedEvent(
                command.getId(),
                command.getCustomerName(),
                command.getCurrency(),
                command.getInitialBalance(),
                AccountStatus.CREATED
        ));
    }

    @CommandHandler
    public void handle(CreditAccountCommand command) {
        log.info("CreditAccountCommand recue : {}", command);
        if (command.getAmount() <= 0) {
            throw new NegativeAmountException("Le montant du credit doit etre strictement positif");
        }
        assertAccountIsActivated();
        AggregateLifecycle.apply(new AccountCreditedEvent(
                command.getId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }

    @CommandHandler
    public void handle(DebitAccountCommand command) {
        log.info("DebitAccountCommand recue : {}", command);
        if (command.getAmount() <= 0) {
            throw new NegativeAmountException("Le montant du debit doit etre strictement positif");
        }
        assertAccountIsActivated();
        if (command.getAmount() > this.balance) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant : solde = " + this.balance + ", montant demande = " + command.getAmount());
        }
        AggregateLifecycle.apply(new AccountDebitedEvent(
                command.getId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }

    @CommandHandler
    public void handle(UpdateAccountStatusCommand command) {
        log.info("UpdateAccountStatusCommand recue : {}", command);
        if (command.getStatus() == null) {
            throw new IllegalArgumentException("Le nouveau statut est obligatoire");
        }
        AggregateLifecycle.apply(new AccountStatusUpdatedEvent(
                command.getId(),
                command.getStatus()
        ));
    }

    // ------------------------------------------------------------------
    // Fonction d'evolution : (State, Event) => State
    // ------------------------------------------------------------------

    @EventSourcingHandler
    public void on(AccountCreatedEvent event) {
        log.info("AccountCreatedEvent rejoue");
        this.accountId = event.getId();
        this.customerName = event.getCustomerName();
        this.currency = event.getCurrency();
        this.balance = event.getBalance();
        this.status = event.getStatus();
        // Le compte est active automatiquement apres sa creation : la decision
        // d'agir sur soi-meme est exprimee par un nouvel evenement.
        AggregateLifecycle.apply(new AccountActivatedEvent(event.getId(), AccountStatus.ACTIVATED));
    }

    @EventSourcingHandler
    public void on(AccountActivatedEvent event) {
        log.info("AccountActivatedEvent rejoue");
        this.status = event.getStatus();
    }

    @EventSourcingHandler
    public void on(AccountCreditedEvent event) {
        log.info("AccountCreditedEvent rejoue");
        this.balance += event.getAmount();
    }

    @EventSourcingHandler
    public void on(AccountDebitedEvent event) {
        log.info("AccountDebitedEvent rejoue");
        this.balance -= event.getAmount();
    }

    @EventSourcingHandler
    public void on(AccountStatusUpdatedEvent event) {
        log.info("AccountStatusUpdatedEvent rejoue");
        this.status = event.getStatus();
    }

    private void assertAccountIsActivated() {
        if (this.status != AccountStatus.ACTIVATED) {
            throw new AccountNotActivatedException("Le compte " + this.accountId + " n'est pas actif (statut = " + this.status + ")");
        }
    }
}
