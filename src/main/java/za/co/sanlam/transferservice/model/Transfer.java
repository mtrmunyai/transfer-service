package za.co.sanlam.transferservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String fromAccountId;

    @Column(nullable = false)
    private String toAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private TransferStatus status;

    // One Transfer to Many Ledger entries
    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL)
    private List<Ledger> ledgers;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;
}
