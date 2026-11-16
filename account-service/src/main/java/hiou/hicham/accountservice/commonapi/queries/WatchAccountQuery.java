package hiou.hicham.accountservice.commonapi.queries;

/**
 * Requete de souscription (subscription query) utilisee pour le suivi temps reel
 * des operations d'un compte. Le resultat initial est la derniere operation connue,
 * les mises a jour sont poussees par le QueryUpdateEmitter a chaque nouvel evenement.
 */
public record WatchAccountQuery(String accountId) {
}
