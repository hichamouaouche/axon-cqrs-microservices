package hiou.hicham.accountservice.commonapi.events;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import lombok.Getter;
import lombok.ToString;

/**
 * Le compte a ete active juste apres sa creation : il accepte desormais les operations.
 */
@Getter
@ToString(callSuper = true)
public class AccountActivatedEvent extends BaseEvent<String> {

    private final AccountStatus status;

    public AccountActivatedEvent(String id,
                                 AccountStatus status) {
        super(id);
        this.status = status;
    }
}
