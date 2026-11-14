package hiou.hicham.accountservice.query.entities;

import hiou.hicham.accountservice.commonapi.enums.AccountStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Projection relationnelle d'un compte : modele de lecture alimente par les Event Handlers.
 * Cette table ne fait jamais autorite, elle peut etre reconstruite en rejouant l'Event Store.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    private String id;

    @Column(nullable = false)
    private String customerName;

    private String currency;

    private double balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private Instant createdAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @OrderBy("operationDate ASC")
    @Builder.Default
    private List<AccountOperation> operations = new ArrayList<>();
}
