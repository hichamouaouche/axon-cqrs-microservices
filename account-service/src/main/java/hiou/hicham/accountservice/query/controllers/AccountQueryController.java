package hiou.hicham.accountservice.query.controllers;

import hiou.hicham.accountservice.commonapi.dtos.AccountResponseDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountStatementDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountWatchEventDTO;
import hiou.hicham.accountservice.commonapi.queries.GetAccountByIdQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAccountStatementQuery;
import hiou.hicham.accountservice.commonapi.queries.GetAllAccountsQuery;
import hiou.hicham.accountservice.commonapi.queries.WatchAccountQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cote lecture (read side) : expose les consultations.
 * <p>
 * Le controleur depose les requetes sur le Query Bus via le {@link QueryGateway} ;
 * il ignore totalement ou et comment les donnees sont stockees.
 */
@RestController
@RequestMapping("/query/accounts")
@Tag(name = "Account Queries", description = "Cote lecture : consultation des comptes, des releves et suivi temps reel")
@RequiredArgsConstructor
@Slf4j
public class AccountQueryController {

    private final QueryGateway queryGateway;

    @GetMapping
    @Operation(summary = "Lister tous les comptes")
    public CompletableFuture<List<AccountResponseDTO>> accounts() {
        return queryGateway.query(new GetAllAccountsQuery(),
                ResponseTypes.multipleInstancesOf(AccountResponseDTO.class));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consulter un compte par son identifiant")
    public CompletableFuture<AccountResponseDTO> accountById(@PathVariable String accountId) {
        return queryGateway.query(new GetAccountByIdQuery(accountId),
                ResponseTypes.instanceOf(AccountResponseDTO.class));
    }

    @GetMapping("/{accountId}/statement")
    @Operation(summary = "Consulter le releve (historique des operations) d'un compte")
    public CompletableFuture<AccountStatementDTO> accountStatement(@PathVariable String accountId) {
        return queryGateway.query(new GetAccountStatementQuery(accountId),
                ResponseTypes.instanceOf(AccountStatementDTO.class));
    }

    /**
     * Suivi temps reel : une subscription query ouvre un flux Server-Sent Events.
     * Le premier element est le resultat initial, les suivants sont les mises a jour
     * poussees par le QueryUpdateEmitter a chaque credit ou debit.
     */
    @GetMapping(value = "/{accountId}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Suivre en temps reel les operations d'un compte (SSE)")
    public Flux<AccountWatchEventDTO> watchAccount(@PathVariable String accountId) {
        SubscriptionQueryResult<AccountWatchEventDTO, AccountWatchEventDTO> result =
                queryGateway.subscriptionQuery(
                        new WatchAccountQuery(accountId),
                        ResponseTypes.instanceOf(AccountWatchEventDTO.class),
                        ResponseTypes.instanceOf(AccountWatchEventDTO.class));
        return result.initialResult()
                .concatWith(result.updates())
                .doFinally(signal -> result.close());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        log.error("Echec du traitement de la requete : {}", cause.getMessage());
        return new ResponseEntity<>(cause.getMessage(), HttpStatus.NOT_FOUND);
    }
}
