package hiou.hicham.accountservice.commands.exceptions;

/**
 * Levee par l'agregat lorsque le solde du compte ne couvre pas le montant a debiter.
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
