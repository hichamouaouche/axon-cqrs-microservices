package hiou.hicham.accountservice.commonapi.dtos;

import hiou.hicham.accountservice.commonapi.enums.OperationType;

import java.time.Instant;

/**
 * Message pousse vers le navigateur (Server-Sent Events) a chaque nouvelle operation.
 */
public record AccountWatchEventDTO(Instant instant,
                                   String accountId,
                                   double currentBalance,
                                   OperationType type,
                                   double operationAmount) {
}
