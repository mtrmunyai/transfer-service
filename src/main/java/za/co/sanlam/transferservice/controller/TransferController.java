package za.co.sanlam.transferservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.sanlam.transferservice.dto.TransferDTO;
import za.co.sanlam.transferservice.service.TransferService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer API", description = "Handles single and batch transfers")
public class TransferController {

  private final TransferService transferService;

  @PostMapping
  @Operation(
      summary = "Create a single transfer",
      description = "Creates a new transfer with the given request data",
      responses = {
        @ApiResponse(responseCode = "200", description = "Transfer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  public ResponseEntity<String> createTransfer(@Valid @RequestBody TransferDTO request) {
    log.info("Received request: {}", request);
    String status = transferService.createTransfer(request);
    log.info("Transfer status: {}", status);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/batch")
  @Operation(
      summary = "Create a batch of transfers",
      description = "Creates multiple transfers in one call",
      responses = {
        @ApiResponse(responseCode = "200", description = "Batch processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  public ResponseEntity<List<String>> createBatch(
      @Valid @RequestBody List<@Valid TransferDTO> requests) {
    log.info("Received request: {}", requests);
    return ResponseEntity.ok(transferService.createBatch(requests));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get transfer status",
      description = "Fetches the status of a transfer by its ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Status fetched successfully"),
        @ApiResponse(responseCode = "404", description = "Transfer not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  public ResponseEntity<String> getTransfer(@PathVariable String id) {
    log.info("Received request: {}", id);
    return ResponseEntity.ok(transferService.getStatusByTransferId(id));
  }
}
