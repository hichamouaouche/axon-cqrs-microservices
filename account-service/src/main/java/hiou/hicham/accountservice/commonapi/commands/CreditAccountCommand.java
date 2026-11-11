package hiou.hicham.accountservice.commonapi.commands;

import lombok.Getter;
import lombok.ToString;

/**
 * Intention de crediter un compte existant.
 */
@Getter
@ToString(callSuper = true)
public class CreditAccountCommand extends BaseCommand<String> {

    private final String currency;
    private final double amount;

    public CreditAccountCommand(String id, String currency, double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}
