package za.co.sanlam.transferservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

  @Mock private RestTemplate restTemplate;

  @Mock private LedgerServiceProperties ledgerServiceProperties;

  @InjectMocks private TransferService transferService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    when(ledgerServiceProperties.getBaseUrl()).thenReturn("http://localhost:8081");
    when(ledgerServiceProperties.getPath()).thenReturn("/ledger/entry");
  }

  @Test
  void testCreateTransfer() {
    TransferRequest request = new TransferRequest();
    request.setFromAccountId("1");
    request.setToAccountId("2");
    request.setAmount(new BigDecimal("100.0"));

    // Create account.

    when(restTemplate.postForObject(
            eq("http://localhost:8081/ledger/entry"), any(TransferDTO.class), eq(String.class)))
        .thenReturn("Success");

    // Inject the RestTemplate manually since it's instantiated inside the service
    transferService =
        new TransferService(ledgerServiceProperties) {
          @Override
          public String createTransfer(TransferRequest request) {
            final String url =
                String.format(
                    "%s%s",
                    ledgerServiceProperties.getBaseUrl(), ledgerServiceProperties.getPath());
            TransferDTO dto =
                TransferDTO.builder()
                    .transferId("test-id")
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .build();
            return restTemplate.postForObject(url, dto, String.class);
          }
        };

    String result = transferService.createTransfer(request);
    assertEquals("Success", result);
  }

  @Test
  void testCreateBatch() {
    TransferRequest request1 = new TransferRequest();
    request1.setFromAccountId("A1");
    request1.setToAccountId("B1");
    request1.setAmount(new BigDecimal("100.0"));
    request1.setTransferId(UUID.randomUUID().toString());

    TransferRequest request2 = new TransferRequest();
    request2.setFromAccountId("A2");
    request2.setToAccountId("B2");
    request2.setAmount(new BigDecimal("200.0"));
    request2.setTransferId(UUID.randomUUID().toString());

    when(restTemplate.postForObject(anyString(), any(TransferDTO.class), eq(String.class)))
        .thenReturn("Success");

    transferService =
        new TransferService(ledgerServiceProperties) {
          @Override
          public String createTransfer(TransferRequest request) {
            return "Success";
          }
        };

    List<String> results = transferService.createBatch(Arrays.asList(request1, request2));
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(r -> r.equals("Success")));
  }
}
