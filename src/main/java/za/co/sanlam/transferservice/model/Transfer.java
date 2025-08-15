package za.co.sanlam.transferservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ToString
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

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    /**
     * Updates transfer status
     *
     * @param status
     */
    public void update(TransferStatus status) {
        this.setStatus(status);
    }
}
