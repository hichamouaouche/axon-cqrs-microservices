package hiou.hicham.accountservice.commonapi.events;

import lombok.Getter;
import lombok.ToString;

/**
 * Un montant a ete debite du compte.
 */
@Getter
@ToString(callSuper = true)
public class AccountDebitedEvent extends BaseEvent<String> {

    private final String currency;
    private final double amount;

    public AccountDebitedEvent(String id,
                               String currency,
                               double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}
