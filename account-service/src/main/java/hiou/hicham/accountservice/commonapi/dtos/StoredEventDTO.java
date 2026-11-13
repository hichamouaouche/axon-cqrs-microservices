package hiou.hicham.accountservice.commonapi.dtos;

import java.time.Instant;

/**
 * Ligne de l'Event Store telle qu'elle est exposee par l'endpoint de consultation
 * des evenements d'un agregat : numero de sequence, type d'evenement, horodatage
 * et payload de l'evenement.
 */
public record StoredEventDTO(long sequenceNumber,
                             String eventType,
                             Instant timestamp,
                             Object payload) {
}
