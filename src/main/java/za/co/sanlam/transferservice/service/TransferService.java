package za.co.sanlam.transferservice.service;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.properties.LedgerServiceProperties;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransferService {

  private final LedgerServiceProperties properties;
  private final RestTemplate restTemplate = new RestTemplate();

  @Autowired
  public TransferService(LedgerServiceProperties properties) {
    this.properties = properties;
  }

  public String createTransfer(TransferRequest request) {

    final String url = String.format("%s%s", properties.getBaseUrl(), properties.getPath());
    final TransferDTO transfer =
        TransferDTO.builder()
            .transferId(UUID.randomUUID().toString())
            .fromAccountId(request.getFromAccountId())
            .toAccountId(request.getToAccountId())
            .amount(request.getAmount())
            .build();

    String status = restTemplate.postForObject(url, transfer, String.class);

    return status;
  }

  public List<String> createBatch(List<TransferRequest> requests) {
    if (requests.size() > 20) {
      throw new ValidationException("Transfer batch size is greater than allow max of 20");
    }

    return requests.stream().map(this::createTransfer).collect(Collectors.toList());
  }

  public String getStatusByTransferId(String transferId) {
    final String url = String.format("%s/ledger/transfer", properties.getBaseUrl());
    final String status = restTemplate.getForObject(url, String.class);

    return status;
  }
}
