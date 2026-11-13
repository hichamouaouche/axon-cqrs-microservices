package hiou.hicham.accountservice.commonapi.dtos;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Charge utile HTTP de changement de statut d'un compte.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequestDTO {

    @NotBlank(message = "L'identifiant du compte est obligatoire")
    private String accountId;

    @NotNull(message = "Le nouveau statut est obligatoire")
    private AccountStatus status;
}
