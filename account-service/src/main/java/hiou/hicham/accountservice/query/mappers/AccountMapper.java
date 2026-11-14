package hiou.hicham.accountservice.query.mappers;

import hiou.hicham.accountservice.commonapi.dtos.AccountOperationDTO;
import hiou.hicham.accountservice.commonapi.dtos.AccountResponseDTO;
import hiou.hicham.accountservice.query.entities.Account;
import hiou.hicham.accountservice.query.entities.AccountOperation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduit les entites de la base de lecture en DTO exposes par l'API de consultation.
 * Les entites JPA ne sortent jamais du cote lecture : le contrat REST reste stable
 * meme si la projection change.
 */
@Component
public class AccountMapper {

    public AccountResponseDTO toAccountDTO(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getCustomerName(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    public AccountOperationDTO toOperationDTO(AccountOperation operation) {
        return new AccountOperationDTO(
                operation.getId(),
                operation.getOperationDate(),
                operation.getType(),
                operation.getAmount()
        );
    }

    public List<AccountResponseDTO> toAccountDTOList(List<Account> accounts) {
        return accounts.stream().map(this::toAccountDTO).toList();
    }

    public List<AccountOperationDTO> toOperationDTOList(List<AccountOperation> operations) {
        return operations.stream().map(this::toOperationDTO).toList();
    }
}
