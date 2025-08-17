package za.co.sanlam.transferservice.service;

import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.model.TransferStatus;

import java.util.List;
import java.util.stream.Collectors;

public interface TransferFallback {
  default List<String> fallbackCreateBatchTransfer(List<TransferDTO> requests, Throwable t) {
    return requests.stream().map(req -> TransferStatus.FAILED.name()).collect(Collectors.toList());
  }

  default String fallbackCreateTransfer(TransferDTO request, Throwable t) {
    return TransferStatus.FAILED.name();
  }

  default String fallbackGetStatus(String transferId, Throwable t) {
    return TransferStatus.UNKNOWN.name();
  }
}
