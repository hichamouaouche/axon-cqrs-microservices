package hiou.hicham.accountservice.commonapi.dtos;

import hiou.hicham.accountservice.commonapi.enums.OperationType;

import java.time.Instant;

/**
 * Representation d'une operation (ligne du releve).
 */
public record AccountOperationDTO(Long id,
                                  Instant operationDate,
                                  OperationType type,
                                  double amount) {
}
