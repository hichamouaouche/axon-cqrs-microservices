package hiou.hicham.accountservice.commonapi.commands;

import lombok.Getter;
import lombok.ToString;

/**
 * Intention de debiter un compte existant.
 */
@Getter
@ToString(callSuper = true)
public class DebitAccountCommand extends BaseCommand<String> {

    private final String currency;
    private final double amount;

    public DebitAccountCommand(String id, String currency, double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}
