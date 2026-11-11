package hiou.hicham.accountservice.commonapi.commands;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import lombok.Getter;
import lombok.ToString;

/**
 * Intention de changer le statut d'un compte (suspension, reactivation, cloture).
 */
@Getter
@ToString(callSuper = true)
public class UpdateAccountStatusCommand extends BaseCommand<String> {

    private final AccountStatus status;

    public UpdateAccountStatusCommand(String id, AccountStatus status) {
        super(id);
        this.status = status;
    }
}
