package hiou.hicham.accountservice.commands.exceptions;

/**
 * Levee par l'agregat lorsqu'un montant negatif est soumis.
 */
public class NegativeAmountException extends RuntimeException {
    public NegativeAmountException(String message) {
        super(message);
    }
}
