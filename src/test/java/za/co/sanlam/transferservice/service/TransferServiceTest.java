package za.co.sanlam.transferservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.exception.RecordNotFoundException;
import za.co.sanlam.transferservice.model.Transfer;
import za.co.sanlam.transferservice.model.TransferStatus;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;
import za.co.sanlam.transferservice.repository.TransferRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class TransferServiceTest {

  @Mock private LedgerServiceProperties properties;
  @Mock private TransferRepository transferRepository;
  @Mock private RestTemplate restTemplate;

  private Executor transferExecutor = Executors.newSingleThreadExecutor();

  private TransferService transferService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    transferService = new TransferService(properties, transferRepository, transferExecutor, restTemplate);
  }

  // ---------------- createTransfer -----------------

  @Test
  void createTransfer_shouldReturnStatusAndSaveTransfer() {
    TransferRequest request = TransferRequest.builder()
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(100))
            .build();

    when(properties.getBaseUrl()).thenReturn("http://ledger");
    when(properties.getPath()).thenReturn("/transfers");
    when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("SUCCESS");

    String status = transferService.createTransfer(request);

    assertEquals("SUCCESS", status);
    verify(transferRepository, times(2)).save(any(Transfer.class));
  }

  @Test
  void createTransfer_shouldUseFallbackOnRestTemplateException() {
    TransferRequest request = TransferRequest.builder()
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(100))
            .build();

    when(properties.getBaseUrl()).thenReturn("http://ledger");
    when(properties.getPath()).thenReturn("/transfers");
    when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenThrow(new RestClientException("Service down"));

    // Since fallback is called on circuit breaker failure, call fallback manually:
    String fallbackStatus = transferService.fallbackCreateTransfer(request, new RestClientException("Service down"));

    assertEquals(TransferStatus.FAILED.name(), fallbackStatus);
  }

  // ---------------- createBatch -----------------

  @Test
  void createBatch_shouldReturnStatuses() {
    TransferRequest req1 = TransferRequest.builder()
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(10))
            .build();
    TransferRequest req2 = TransferRequest.builder()
            .fromAccountId("3")
            .toAccountId("4")
            .amount(BigDecimal.valueOf(20))
            .build();

    when(properties.getBaseUrl()).thenReturn("http://ledger");
    when(properties.getPath()).thenReturn("/transfers");
    when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("SUCCESS");

    List<String> results = transferService.createBatch(Arrays.asList(req1, req2));

    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch("SUCCESS"::equals));
  }

  @Test
  void createBatch_shouldThrowValidationException_forEmptyList() {
    ValidationException ex = assertThrows(ValidationException.class,
        () -> transferService.createBatch(Collections.emptyList()));
    assertTrue(ex.getMessage().contains("Invalid batch size"));
  }

  @Test
  void createBatch_shouldThrowValidationException_forTooLargeList() {
    List<TransferRequest> largeList = new ArrayList<>();
    for (int i = 0; i < 21; i++) largeList.add(new TransferRequest());

    ValidationException ex = assertThrows(ValidationException.class,
        () -> transferService.createBatch(largeList));
    assertTrue(ex.getMessage().contains("greater than allow max"));
  }

  @Test
  void fallbackCreateBatchTransfer_shouldReturnFailedStatuses() {
    TransferRequest req1 = TransferRequest.builder().transferId("t1").build();
    TransferRequest req2 = TransferRequest.builder().transferId("t2").build();

    List<String> fallbackResults = transferService.fallbackCreateBatchTransfer(
            Arrays.asList(req1, req2), new RuntimeException("boom"));

    assertEquals(2, fallbackResults.size());
    assertTrue(fallbackResults.contains("t1:FAILED"));
    assertTrue(fallbackResults.contains("t2:FAILED"));
  }

  // ---------------- getStatusByTransferId -----------------

  @Test
  void getStatusByTransferId_shouldReturnStatus() {
    String id = UUID.randomUUID().toString();
    Transfer transfer = Transfer.builder()
            .id(id)
            .status(TransferStatus.SUCCESS)
            .build();

    when(transferRepository.findById(id)).thenReturn(Optional.of(transfer));

    String status = transferService.getStatusByTransferId(id);

    assertEquals("SUCCESS", status);
  }

  @Test
  void getStatusByTransferId_shouldThrowRecordNotFoundException() {
    String id = UUID.randomUUID().toString();
    when(transferRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(RecordNotFoundException.class,
        () -> transferService.getStatusByTransferId(id));
  }

  @Test
  void fallbackGetStatus_shouldReturnUnknown() {
    String fallback = transferService.fallbackGetStatus("someId", new RuntimeException("fail"));
    assertEquals(TransferStatus.UNKNOWN.name(), fallback);
  }
}
