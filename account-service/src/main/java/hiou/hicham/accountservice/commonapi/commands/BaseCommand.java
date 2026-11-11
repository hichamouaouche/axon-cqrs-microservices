package hiou.hicham.accountservice.commonapi.commands;

import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Classe de base de toutes les commandes de l'application.
 * <p>
 * L'identifiant porte l'annotation {@link TargetAggregateIdentifier} : c'est grace a elle que
 * le Command Bus d'Axon sait vers quelle instance d'agregat router la commande.
 * Une commande est immuable : elle n'expose que des getters.
 *
 * @param <T> type de l'identifiant de l'agregat cible
 */
@ToString
public abstract class BaseCommand<T> {

    @TargetAggregateIdentifier
    @Getter
    private final T id;

    protected BaseCommand(T id) {
        this.id = id;
    }
}
