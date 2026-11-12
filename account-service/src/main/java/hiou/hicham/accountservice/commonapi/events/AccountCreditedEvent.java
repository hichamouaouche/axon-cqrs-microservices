package hiou.hicham.accountservice.commonapi.events;

import lombok.Getter;
import lombok.ToString;

/**
 * Un montant a ete credite sur le compte.
 */
@Getter
@ToString(callSuper = true)
public class AccountCreditedEvent extends BaseEvent<String> {

    private final String currency;
    private final double amount;

    public AccountCreditedEvent(String id,
                                String currency,
                                double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}
