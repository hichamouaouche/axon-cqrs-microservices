package hiou.hicham.accountservice.commonapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Charge utile HTTP d'une operation de debit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitAccountRequestDTO {

    @NotBlank(message = "L'identifiant du compte est obligatoire")
    private String accountId;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;

    @Positive(message = "Le montant doit etre strictement positif")
    private double amount;
}
