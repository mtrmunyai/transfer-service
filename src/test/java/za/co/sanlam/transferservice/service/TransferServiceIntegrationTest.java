package za.co.sanlam.transferservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.model.Transfer;
import za.co.sanlam.transferservice.model.TransferStatus;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;
import za.co.sanlam.transferservice.repository.TransferRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureTestDatabase
class TransferServiceIntegrationTest {

  @Autowired private TransferService transferService;

  @Autowired private TransferRepository transferRepository;

  @Autowired private RestTemplate restTemplate;

  @Autowired private LedgerServiceProperties properties;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setup() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    transferRepository.deleteAll();
  }

  @Test
  void createTransfer_shouldPersistAndReturnStatus() {
    // Arrange
    String expectedStatus = TransferStatus.SUCCESS.name();
    mockServer
        .expect(once(), requestTo(properties.getBaseUrl() + properties.getPath()))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(expectedStatus, MediaType.APPLICATION_JSON));

    TransferDTO request =
        TransferDTO.builder()
            .fromAccountId("A123")
            .toAccountId("B456")
            .amount(BigDecimal.valueOf(100.50))
            .build();

    // Act
    String status = transferService.createTransfer(request);

    // Assert
    assertThat(status).isEqualTo(expectedStatus);

    List<Transfer> transfers = transferRepository.findAll();
    assertThat(transfers).hasSize(1);
    assertThat(transfers.get(0).getStatus()).isEqualTo(TransferStatus.SUCCESS);

    mockServer.verify();
  }

  @Test
  void createBatch_shouldHandleMultipleTransfers() {
    // Arrange
    String expectedStatus = TransferStatus.SUCCESS.name();
    mockServer
        .expect(once(), requestTo(properties.getBaseUrl() + properties.getPath()))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(expectedStatus, MediaType.APPLICATION_JSON));

    mockServer
        .expect(once(), requestTo(properties.getBaseUrl() + properties.getPath()))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(expectedStatus, MediaType.APPLICATION_JSON));

    TransferDTO r1 =
        TransferDTO.builder()
            .fromAccountId("A111")
            .toAccountId("B111")
            .amount(BigDecimal.valueOf(50))
            .build();

    TransferDTO r2 =
        TransferDTO.builder()
            .fromAccountId("A222")
            .toAccountId("B222")
            .amount(BigDecimal.valueOf(75))
            .build();

    // Act
    List<String> results = transferService.createBatch(Arrays.asList(r1, r2));

    // Assert
    assertThat(results).containsExactly(expectedStatus, expectedStatus);

    List<Transfer> transfers = transferRepository.findAll();
    assertThat(transfers).hasSize(2);
    assertThat(transfers).allMatch(t -> t.getStatus() == TransferStatus.SUCCESS);

    mockServer.verify();
  }

  @Test
  void getStatusByTransferId_shouldReturnPersistedStatus() {
    // Arrange
    String expectedStatus = TransferStatus.SUCCESS.name();
    mockServer
        .expect(once(), requestTo(properties.getBaseUrl() + properties.getPath()))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(expectedStatus, MediaType.APPLICATION_JSON));

    TransferDTO request =
        TransferDTO.builder().fromAccountId("X1").toAccountId("Y1").amount(BigDecimal.TEN).build();

    String status = transferService.createTransfer(request);
    assertThat(status).isEqualTo(expectedStatus);

    String transferId = transferRepository.findAll().get(0).getId();

    // Act
    String retrieved = transferService.getStatusByTransferId(transferId);

    // Assert
    assertThat(retrieved).isEqualTo(expectedStatus);
  }
}
