package za.co.sanlam.transferservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.sanlam.transferservice.model.Transfer;

import java.time.LocalDateTime;

public interface TransferRepository extends JpaRepository<Transfer, String> {
    void deleteByCreatedBefore(LocalDateTime cutoff);

}
