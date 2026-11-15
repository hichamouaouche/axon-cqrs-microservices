package hiou.hicham.accountservice.query.services;

import hiou.hicham.accountservice.commonapi.enums.OperationType;
import hiou.hicham.accountservice.commonapi.events.AccountActivatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreatedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountCreditedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountDebitedEvent;
import hiou.hicham.accountservice.commonapi.events.AccountStatusUpdatedEvent;
import hiou.hicham.accountservice.query.entities.Account;
import hiou.hicham.accountservice.query.entities.AccountOperation;
import hiou.hicham.accountservice.query.repositories.AccountOperationRepository;
import hiou.hicham.accountservice.query.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Projection des evenements dans la base de lecture.
 * <p>
 * Chaque {@code @EventHandler} recoit un evenement de l'Event Bus et met a jour le modele
 * relationnel. La base de lecture peut etre reconstruite a tout moment en rejouant
 * les evenements depuis le debut.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountEventHandler {

    private final AccountRepository accountRepository;
    private final AccountOperationRepository accountOperationRepository;

    @EventHandler
    public void on(AccountCreatedEvent event, EventMessage<AccountCreatedEvent> message) {
        log.info("Projection : AccountCreatedEvent recu pour le compte {}", event.getId());
        Account account = Account.builder()
                .id(event.getId())
                .customerName(event.getCustomerName())
                .currency(event.getCurrency())
                .balance(event.getBalance())
                .status(event.getStatus())
                .createdAt(message.getTimestamp())
                .build();
        accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountActivatedEvent event) {
        log.info("Projection : AccountActivatedEvent recu pour le compte {}", event.getId());
        accountRepository.findById(event.getId()).ifPresent(account -> {
            account.setStatus(event.getStatus());
            accountRepository.save(account);
        });
    }

    @EventHandler
    public void on(AccountCreditedEvent event, EventMessage<AccountCreditedEvent> message) {
        log.info("Projection : AccountCreditedEvent recu pour le compte {}", event.getId());
        Account account = accountRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalStateException("Compte introuvable dans la projection : " + event.getId()));
        AccountOperation operation = AccountOperation.builder()
                .account(account)
                .amount(event.getAmount())
                .type(OperationType.CREDIT)
                .operationDate(message.getTimestamp())
                .build();
        accountOperationRepository.save(operation);
        account.setBalance(account.getBalance() + event.getAmount());
        accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountDebitedEvent event, EventMessage<AccountDebitedEvent> message) {
        log.info("Projection : AccountDebitedEvent recu pour le compte {}", event.getId());
        Account account = accountRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalStateException("Compte introuvable dans la projection : " + event.getId()));
        AccountOperation operation = AccountOperation.builder()
                .account(account)
                .amount(event.getAmount())
                .type(OperationType.DEBIT)
                .operationDate(message.getTimestamp())
                .build();
        accountOperationRepository.save(operation);
        account.setBalance(account.getBalance() - event.getAmount());
        accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountStatusUpdatedEvent event) {
        log.info("Projection : AccountStatusUpdatedEvent recu pour le compte {}", event.getId());
        accountRepository.findById(event.getId()).ifPresent(account -> {
            account.setStatus(event.getStatus());
            accountRepository.save(account);
        });
    }

}
