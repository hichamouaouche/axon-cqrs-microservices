package hiou.hicham.accountservice.commonapi.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public AccountStatusUpdatedEvent(@JsonProperty("id") String id,
                                     @JsonProperty("status") AccountStatus status) {
        super(id);
        this.status = status;
    }
}
