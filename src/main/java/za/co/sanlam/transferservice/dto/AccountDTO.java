package za.co.sanlam.transferservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Details of an account, including ID and current balance")
public class AccountDTO {
    @Schema(description = "Unique account identifier", example = "1")
    private Long id;

    @Schema(description = "Current balance in the account", example = "2500.75", minimum = "0.01")
    private BigDecimal balance;
}
