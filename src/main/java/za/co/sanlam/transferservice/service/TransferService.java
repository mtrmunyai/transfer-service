package za.co.sanlam.transferservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.exception.RecordNotFoundException;
import za.co.sanlam.transferservice.model.Transfer;
import za.co.sanlam.transferservice.model.TransferStatus;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;
import za.co.sanlam.transferservice.repository.TransferRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransferService {

  private final LedgerServiceProperties properties;
  private final RestTemplate restTemplate;
  private final TransferRepository transferRepository;
  private final Executor transferExecutor;

  @Autowired
  public TransferService(LedgerServiceProperties properties,
                         TransferRepository transferRepository,
                         @Qualifier("transferExecutor") Executor transferExecutor,
                         RestTemplate restTemplate) {
    this.properties = properties;
    this.transferRepository = transferRepository;
    this.transferExecutor = transferExecutor;
    this.restTemplate = restTemplate;
  }

  @Transactional
  @CircuitBreaker(name = "ledgerService", fallbackMethod = "fallbackCreateTransfer")
  public String createTransfer(TransferRequest request) {

    final String url = String.format("%s%s", properties.getBaseUrl(), properties.getPath());
    final String transferId = UUID.randomUUID().toString();

    final TransferDTO transfer =
            TransferDTO.builder()
                    .transferId(transferId)
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .build();

    log.info("Create Transfer: url: {}, request: {}", url, transfer);

    final Transfer transferEntity = Transfer
            .builder()
            .id(transferId)
            .amount(request.getAmount())
            .toAccountId(request.getToAccountId())
            .fromAccountId(request.getFromAccountId())
            .status(TransferStatus.UNKNOWN)
            .build();

    final Transfer pendingTransfer = transferRepository.save(transferEntity);

    final String status = restTemplate.postForObject(url, transfer, String.class);

    log.info("Transfer status: {}", status);

    pendingTransfer.setStatus(TransferStatus.valueOf(status));

    transferRepository.save(pendingTransfer);

    return status;
  }

  @Transactional
  @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackCreateBatchTransfer")
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

    List<CompletableFuture<String>> futures = requests.stream()
            .map(request -> CompletableFuture.supplyAsync(() -> createTransfer(request), transferExecutor))
            .collect(Collectors.toList());

    return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
  }

  @CircuitBreaker(name = "ledgerService", fallbackMethod = "fallbackGetStatus")
  public String getStatusByTransferId(String transferId) {

    log.info("Get transfer status: {}", transferId);

    final String status = transferRepository.findById(transferId)
            .map(Transfer::getStatus)
            .map(Enum::name)
            .orElseThrow(() -> new RecordNotFoundException("Failed to find transfer using transferId: " + transferId));

    log.info("Transfer status: {}", status);
    return status;
  }

  public List<String> fallbackCreateBatchTransfer(List<TransferRequest> requests, Throwable t) {
    log.error("Failed to create transfer with request: {}, error: {}", requests, t.getMessage());

    return requests
            .stream()
            .map(transferRequest -> String.format("%s:%s", transferRequest.getTransferId(), TransferStatus.FAILED))
            .collect(Collectors.toList());
  }

  public String fallbackCreateTransfer(TransferRequest request, Throwable t) {
    log.error("Failed to create transfer with request: {}, error: {}", request, t.getMessage());
    return TransferStatus.FAILED.name();
  }

  public String fallbackGetStatus(String transferId, Throwable t) {
    log.error("Failed to get transfer status with transferId: {}, error: {}", transferId, t.getMessage());
    return TransferStatus.UNKNOWN.name();
  }
}
