package hiou.hicham.accountservice.commonapi.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public AccountDebitedEvent(@JsonProperty("id") String id,
                               @JsonProperty("currency") String currency,
                               @JsonProperty("amount") double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}
