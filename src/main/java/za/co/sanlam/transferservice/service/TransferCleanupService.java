package za.co.sanlam.transferservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import za.co.sanlam.transferservice.repository.TransferRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferCleanupService {

    private final TransferRepository transferRepository;

    // Runs every hour (adjust as needed)
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupOldTransfers() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        log.info("Cleaning up transfers older than: {}", cutoff);

        transferRepository.deleteByCreatedBefore(cutoff);
    }
}
