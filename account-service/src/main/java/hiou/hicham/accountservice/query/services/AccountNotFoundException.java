package hiou.hicham.accountservice.query.services;

/**
 * Levee par le cote lecture lorsqu'aucune projection ne correspond a l'identifiant demande.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
