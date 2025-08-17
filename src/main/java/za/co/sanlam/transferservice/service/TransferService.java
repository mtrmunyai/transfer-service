package za.co.sanlam.transferservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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
import java.util.Optional;
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

  // Self-injection so async calls use the Spring proxy (AOP applies)
  private final TransferService self;

  @Autowired
  public TransferService(LedgerServiceProperties properties,
                         TransferRepository transferRepository,
                         @Qualifier("transferExecutor") Executor transferExecutor,
                         RestTemplate restTemplate,
                         @Lazy TransferService self) {
    this.properties = properties;
    this.transferRepository = transferRepository;
    this.transferExecutor = transferExecutor;
    this.restTemplate = restTemplate;
    this.self = self;
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @CircuitBreaker(name = "ledgerService", fallbackMethod = "fallbackCreateTransfer")
  public String createTransfer(TransferRequest request) {

    final String url = String.format("%s%s", properties.getBaseUrl(), properties.getPath());
    final String transferId = request.getTransferId()!= null? request.getTransferId(): UUID.randomUUID().toString();

    // Check if transfer has been initiated
     Optional<Transfer> transferOptional = transferRepository.findById(transferId);
    if(transferOptional.isPresent()) {
      log.warn("Transfer: {}, already exist", transferOptional);
      return transferOptional.get().getStatus().name();
    }

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

  @Transactional(Transactional.TxType.SUPPORTS)
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

    // Use self proxy so @Transactional/@CircuitBreaker apply in async threads
    List<CompletableFuture<String>> futures = requests.stream()
            .map(req -> CompletableFuture.supplyAsync(
                    () -> self.createTransfer(req), transferExecutor
            ).exceptionally(ex -> {
              log.error("Async transfer failed for request {}: {}", req, ex.getMessage());
              return TransferStatus.FAILED.name();
            }))
            .collect(Collectors.toList());

    // Join each future; failures already mapped to FAILED
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
            .orElseThrow(() -> new RecordNotFoundException(
                    "Failed to find transfer using transferId: " + transferId));

    log.info("Transfer status: {}", status);
    return status;
  }

  public List<String> fallbackCreateBatchTransfer(List<TransferRequest> requests, Throwable t) {
    log.error("Failed to create transfer with request(s): {}, error: {}", requests, t.getMessage());
    return requests.stream()
            .map(req -> TransferStatus.FAILED.name())
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
