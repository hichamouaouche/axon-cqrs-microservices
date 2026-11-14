package hiou.hicham.accountservice.commonapi.dtos;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;

import java.time.Instant;

/**
 * Representation d'un compte renvoyee par le cote lecture.
 */
public record AccountResponseDTO(String id,
                                 String customerName,
                                 String currency,
                                 double balance,
                                 AccountStatus status,
                                 Instant createdAt) {
}
