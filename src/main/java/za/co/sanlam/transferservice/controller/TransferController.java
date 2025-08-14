package za.co.sanlam.transferservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.service.TransferService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

  private final TransferService transferService;

  @PostMapping
  public ResponseEntity<String> createTransfer(@Valid  @RequestBody TransferRequest request) {
    log.info("Received request: {}", request);

    String status = transferService.createTransfer(request);

    log.info("Transfer status: {}", status);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/batch")
  public ResponseEntity<List<String>> createBatch(
      @RequestBody List<TransferRequest> requests) {
    log.info("Received request: {}", requests);

    return ResponseEntity.ok(transferService.createBatch(requests));
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getTransfer(@PathVariable String id) {
    log.info("Received request: {}", id);

    return ResponseEntity.ok(transferService.getStatusByTransferId(id));
  }
}
