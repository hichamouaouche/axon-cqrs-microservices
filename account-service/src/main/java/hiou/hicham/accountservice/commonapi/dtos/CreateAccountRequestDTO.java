package hiou.hicham.accountservice.commonapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Charge utile HTTP de creation d'un compte.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDTO {

    @NotBlank(message = "Le nom du client est obligatoire")
    private String customerName;

    @NotBlank(message = "La devise est obligatoire")
    private String currency;

    @PositiveOrZero(message = "Le solde initial ne peut pas etre negatif")
    private double initialBalance;
}
