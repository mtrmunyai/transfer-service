package za.co.sanlam.transferservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferTest {

  @Test
  void testBuilder_createsCorrectObject() {
    LocalDateTime now = LocalDateTime.now();
    String transferId = UUID.randomUUID().toString();

    Transfer transfer =
        Transfer.builder()
            .id(transferId)
            .version(2L)
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(150.75))
            .status(TransferStatus.UNKNOWN)
            .created(now)
            .build();

    assertAll(
        "Transfer properties",
        () -> assertEquals(transferId, transfer.getId()),
        () -> assertEquals(2L, transfer.getVersion()),
        () -> assertEquals("1", transfer.getFromAccountId()),
        () -> assertEquals("2", transfer.getToAccountId()),
        () -> assertEquals(BigDecimal.valueOf(150.75), transfer.getAmount()),
        () -> assertEquals(TransferStatus.UNKNOWN, transfer.getStatus()),
        () -> assertEquals(now, transfer.getCreated()));
  }

  @Test
  void testUpdate_setsStatusCorrectly() {
    Transfer transfer = new Transfer();
    transfer.setStatus(TransferStatus.UNKNOWN);

    transfer.update(TransferStatus.SUCCESS);

    assertEquals(TransferStatus.SUCCESS, transfer.getStatus());
  }

  @Test
  void testSettersAndGetters_workCorrectly() {
    LocalDateTime now = LocalDateTime.now();

    Transfer transfer =
        Transfer.builder()
            .id("id-001")
            .version(5L)
            .fromAccountId("fromAcc")
            .toAccountId("toAcc")
            .amount(BigDecimal.TEN)
            .status(TransferStatus.FAILED)
            .created(now)
            .build();

    assertAll(
        "Verify Transfer fields",
        () -> assertEquals("id-001", transfer.getId()),
        () -> assertEquals(5L, transfer.getVersion()),
        () -> assertEquals("fromAcc", transfer.getFromAccountId()),
        () -> assertEquals("toAcc", transfer.getToAccountId()),
        () -> assertEquals(BigDecimal.TEN, transfer.getAmount()),
        () -> assertEquals(TransferStatus.FAILED, transfer.getStatus()),
        () -> assertEquals(now, transfer.getCreated()));
  }
}
