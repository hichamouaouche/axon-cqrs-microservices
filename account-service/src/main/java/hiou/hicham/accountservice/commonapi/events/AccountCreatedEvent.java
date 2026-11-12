package hiou.hicham.accountservice.commonapi.events;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import lombok.Getter;
import lombok.ToString;

/**
 * Un compte bancaire a ete cree.
 */
@Getter
@ToString(callSuper = true)
public class AccountCreatedEvent extends BaseEvent<String> {

    private final String customerName;
    private final String currency;
    private final double balance;
    private final AccountStatus status;

    public AccountCreatedEvent(String id,
                               String customerName,
                               String currency,
                               double balance,
                               AccountStatus status) {
        super(id);
        this.customerName = customerName;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
    }
}
