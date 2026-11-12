package hiou.hicham.accountservice.commands.exceptions;

/**
 * Levee par l'agregat lorsqu'une operation est demandee sur un compte non active.
 */
public class AccountNotActivatedException extends RuntimeException {
    public AccountNotActivatedException(String message) {
        super(message);
    }
}
