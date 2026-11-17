package hiou.hicham.accountservice.commonapi.dtos;

import java.util.List;

/**
 * Conteneur de reponse de la requete GetAllAccountsQuery.
 * <p>
 * Le serialiseur Jackson perd le type des elements d'une collection lorsque la reponse
 * traverse Axon Server : un {@code ResponseTypes.multipleInstancesOf(...)} serait relu
 * comme une liste de Map. Le resultat est donc transporte dans ce type dedie, dont les
 * generiques sont connus a la deserialisation. Le controleur REST, lui, continue
 * d'exposer un simple tableau JSON.
 */
public record AccountListDTO(List<AccountResponseDTO> accounts) {
}
