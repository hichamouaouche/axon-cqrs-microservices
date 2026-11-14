package hiou.hicham.accountservice.commonapi.dtos;

import java.util.List;

/**
 * Releve d'un compte : l'etat courant du compte et l'historique de ses operations.
 */
public record AccountStatementDTO(AccountResponseDTO account,
                                  List<AccountOperationDTO> operations) {
}
