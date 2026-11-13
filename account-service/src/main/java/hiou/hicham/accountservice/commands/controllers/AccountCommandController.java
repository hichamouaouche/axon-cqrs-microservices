package hiou.hicham.accountservice.commands.controllers;

import hiou.hicham.accountservice.commonapi.commands.AddAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.CreditAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.DebitAccountCommand;
import hiou.hicham.accountservice.commonapi.commands.UpdateAccountStatusCommand;
import hiou.hicham.accountservice.commonapi.dtos.CreateAccountRequestDTO;
import hiou.hicham.accountservice.commonapi.dtos.CreditAccountRequestDTO;
import hiou.hicham.accountservice.commonapi.dtos.DebitAccountRequestDTO;
import hiou.hicham.accountservice.commonapi.dtos.StoredEventDTO;
import hiou.hicham.accountservice.commonapi.dtos.UpdateAccountStatusRequestDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Cote commande (write side) : expose les intentions de modification de l'etat de l'application.
 * <p>
 * Le controleur ne contient aucune regle metier. Il transforme la requete HTTP en commande
 * et la depose sur le Command Bus via le {@link CommandGateway}. Axon route ensuite la
 * commande vers le {@code @CommandHandler} de l'agregat cible.
 */
@RestController
@RequestMapping("/commands/accounts")
@Slf4j
public class AccountCommandController {

    private final CommandGateway commandGateway;
    private final EventStore eventStore;

    public AccountCommandController(CommandGateway commandGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @PostMapping("/create")
    public CompletableFuture<String> createAccount(@RequestBody @Valid CreateAccountRequestDTO request) {
        return commandGateway.send(new AddAccountCommand(
                UUID.randomUUID().toString(),
                request.getCustomerName(),
                request.getCurrency(),
                request.getInitialBalance()
        ));
    }

    @PutMapping("/credit")
    public CompletableFuture<String> creditAccount(@RequestBody @Valid CreditAccountRequestDTO request) {
        return commandGateway.send(new CreditAccountCommand(
                request.getAccountId(),
                request.getCurrency(),
                request.getAmount()
        ));
    }

    @PutMapping("/debit")
    public CompletableFuture<String> debitAccount(@RequestBody @Valid DebitAccountRequestDTO request) {
        return commandGateway.send(new DebitAccountCommand(
                request.getAccountId(),
                request.getCurrency(),
                request.getAmount()
        ));
    }

    @PutMapping("/status")
    public CompletableFuture<String> updateStatus(@RequestBody @Valid UpdateAccountStatusRequestDTO request) {
        return commandGateway.send(new UpdateAccountStatusCommand(
                request.getAccountId(),
                request.getStatus()
        ));
    }

    @GetMapping("/eventStore/{accountId}")
    public List<StoredEventDTO> eventStore(@PathVariable String accountId) {
        return eventStore.readEvents(accountId)
                .asStream()
                .map(message -> new StoredEventDTO(
                        message.getSequenceNumber(),
                        message.getPayloadType().getSimpleName(),
                        message.getTimestamp(),
                        message.getPayload()))
                .toList();
    }

    /**
     * Les exceptions metier levees par l'agregat traversent le Command Bus ;
     * elles sont ici traduites en reponse HTTP lisible par le client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        log.error("Echec du traitement de la commande : {}", cause.getMessage());
        return new ResponseEntity<>(cause.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
