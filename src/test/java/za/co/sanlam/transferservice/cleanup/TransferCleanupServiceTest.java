package za.co.sanlam.transferservice.cleanup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import za.co.sanlam.transferservice.repository.TransferRepository;
import za.co.sanlam.transferservice.service.TransferCleanupService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransferCleanupServiceTest {

  private TransferRepository transferRepository;
  private TransferCleanupService transferCleanupService;

  @BeforeEach
  void setUp() {
    transferRepository = Mockito.mock(TransferRepository.class);
    transferCleanupService = new TransferCleanupService(transferRepository);
  }

  @Test
  void cleanupOldTransfers_deletesRecordsOlderThan24Hours() {
    transferCleanupService.cleanupOldTransfers();

    ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(transferRepository, times(1)).deleteByCreatedBefore(captor.capture());

    LocalDateTime cutoff = captor.getValue();
    LocalDateTime nowMinus24Hours = LocalDateTime.now().minusHours(24);

    assertTrue(
        cutoff.isAfter(nowMinus24Hours.minusSeconds(5))
            && cutoff.isBefore(nowMinus24Hours.plusSeconds(5)),
        "Cutoff should be approx 24 hours ago");
  }
}
