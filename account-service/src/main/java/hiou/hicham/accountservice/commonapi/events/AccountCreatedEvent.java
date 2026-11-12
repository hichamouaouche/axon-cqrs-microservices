package hiou.hicham.accountservice.commonapi.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public AccountCreatedEvent(@JsonProperty("id") String id,
                               @JsonProperty("customerName") String customerName,
                               @JsonProperty("currency") String currency,
                               @JsonProperty("balance") double balance,
                               @JsonProperty("status") AccountStatus status) {
        super(id);
        this.customerName = customerName;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
    }
}
