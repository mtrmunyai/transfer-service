package za.co.sanlam.transferservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.service.TransferService;

import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

  private final TransferService transferService;

  @PostMapping
  public ResponseEntity<String> createTransfer(@Valid  @RequestBody TransferRequest request) {
    String status = transferService.createTransfer(request);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/batch")
  public ResponseEntity<List<String>> createBatch(
      @RequestBody List<TransferRequest> requests) {
    return ResponseEntity.ok(transferService.createBatch(requests));
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getTransfer(@PathVariable String id) {
    return ResponseEntity.ok(transferService.getStatusByTransferId(id));
  }
}
