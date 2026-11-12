package hiou.hicham.accountservice.commonapi.events;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import lombok.Getter;
import lombok.ToString;

/**
 * Le statut du compte a change (suspension, reactivation, cloture).
 */
@Getter
@ToString(callSuper = true)
public class AccountStatusUpdatedEvent extends BaseEvent<String> {

    private final AccountStatus status;

    public AccountStatusUpdatedEvent(String id,
                                     AccountStatus status) {
        super(id);
        this.status = status;
    }
}
