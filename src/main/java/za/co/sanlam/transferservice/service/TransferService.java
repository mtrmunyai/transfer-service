package za.co.sanlam.transferservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransferService {

  private final LedgerServiceProperties properties;
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  public TransferService(LedgerServiceProperties properties) {
    this.properties = properties;
  }

  @Transactional
  @CircuitBreaker(name = "ledgerService", fallbackMethod = "fallbackCreateTransfer")
  public String createTransfer(TransferRequest request) {

    final String url = String.format("%s%s", properties.getBaseUrl(), properties.getPath());
    final TransferDTO transfer =
        TransferDTO.builder()
            .transferId(UUID.randomUUID().toString())
            .fromAccountId(request.getFromAccountId())
            .toAccountId(request.getToAccountId())
            .amount(request.getAmount())
            .build();

    log.info("Create Transfer: url: {}, request: {}", url, transfer);

    String status = restTemplate.postForObject(url, transfer, String.class);

    log.info("Transfer status: {}", status);

    return status;
  }

  public List<String> createBatch(List<TransferRequest> requests) {
    if (Objects.isNull(requests) || requests.isEmpty()) {
      log.error("Invalid batch size: nothing to process");
      throw new ValidationException("Invalid batch size: nothing to process");
    }

    if (requests.size() > 20) {
      log.error("Transfer batch size is greater than allow max of 20");
      throw new ValidationException("Transfer batch size is greater than allow max of 20");
    }

    log.info("Batch size: {}", requests.size());

    return requests.stream().map(this::createTransfer).collect(Collectors.toList());
  }

  @CircuitBreaker(name = "ledgerService", fallbackMethod = "fallbackGetStatus")
  public String getStatusByTransferId(String transferId) {
    final String url = String.format("%s/ledger/transfer", properties.getBaseUrl());
    final TransferDTO transferDTO =
        TransferDTO.builder()
            .transferId(transferId)
            .amount(new BigDecimal("0.01"))
            .toAccountId("1")
            .fromAccountId("2")
            .build();

    log.info("Create Transfer: url: {}, request: {}", url, transferDTO);

    final String status = restTemplate.postForObject(url, transferDTO, String.class);

    log.info("Transfer status: {}", status);
    return status;
  }

  public String fallbackCreateTransfer(TransferRequest request, Throwable t) {
    log.error("Failed to create transfer with request: {}, error: {}", request, t.getMessage());

    return "FAILED";
  }

  public String fallbackGetStatus(String transferId, Throwable t) {
    log.error(
        "Failed to get transfer status with transferId: {}, error: {}", transferId, t.getMessage());

    return "UNKNOWN";
  }
}
