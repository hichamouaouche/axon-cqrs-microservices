package hiou.hicham.accountservice.commonapi.events;

import lombok.Getter;
import lombok.ToString;

/**
 * Classe de base de tous les evenements metier.
 * <p>
 * Un evenement est un fait qui s'est deja produit : il est donc immuable et auto-descriptif.
 * Son nom est au passe (AccountCreatedEvent, AccountCreditedEvent, ...).
 * Les evenements sont serialises en JSON dans l'Event Store, les annotations Jackson
 * portees par les constructeurs des sous-classes garantissent la relecture du payload.
 *
 * @param <T> type de l'identifiant de l'agregat concerne
 */
@ToString
public abstract class BaseEvent<T> {

    @Getter
    private final T id;

    protected BaseEvent(T id) {
        this.id = id;
    }
}
