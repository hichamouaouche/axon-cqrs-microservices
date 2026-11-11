package hiou.hicham.accountservice.commonapi.commands;

import lombok.Getter;
import lombok.ToString;

/**
 * Intention de creation d'un nouveau compte bancaire.
 */
@Getter
@ToString(callSuper = true)
public class AddAccountCommand extends BaseCommand<String> {

    private final String customerName;
    private final String currency;
    private final double initialBalance;

    public AddAccountCommand(String id, String customerName, String currency, double initialBalance) {
        super(id);
        this.customerName = customerName;
        this.currency = currency;
        this.initialBalance = initialBalance;
    }
}
