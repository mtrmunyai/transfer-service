package za.co.sanlam.transferservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Data
@Builder
@Schema(description = "Transfer details for initiating a fund transfer")
public class TransferDTO {

  @Schema(
      description = "Unique ID for the transfer",
      example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef")
  @NotNull(message = "Transfer ID cannot be null")
  private String transferId;

  @Schema(description = "Account ID from which funds will be debited", example = "1")
  @NotNull(message = "Transfer from account ID cannot be null")
  private String fromAccountId;

  @Schema(description = "Account ID to which funds will be credited", example = "2")
  @NotNull(message = "Transfer to account ID cannot be null")
  private String toAccountId;

  @Schema(description = "Amount to transfer", example = "150.75")
  @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
  @NotNull(message = "Amount cannot be null")
  private BigDecimal amount;
}
