package za.co.sanlam.transferservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransferDTOTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void whenAllFieldsAreValid_thenNoValidationErrors() {
    TransferDTO request =
        TransferDTO.builder()
            .transferId("a1b2c3d4-e5f6-7890-abcd-1234567890ef")
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(150.75))
            .build();

    Set<ConstraintViolation<TransferDTO>> violations = validator.validate(request);
    assertTrue(violations.isEmpty(), "Expected no validation errors");
  }

  @Test
  void whenFromAccountIdIsNull_thenValidationFails() {
    TransferDTO request =
        TransferDTO.builder()
            .transferId("some-id")
            .fromAccountId(null)
            .toAccountId("2")
            .amount(BigDecimal.valueOf(100.00))
            .build();

    Set<ConstraintViolation<TransferDTO>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fromAccountId")));
  }

  @Test
  void whenToAccountIdIsNull_thenValidationFails() {
    TransferDTO request =
        TransferDTO.builder()
            .transferId("some-id")
            .fromAccountId("1")
            .toAccountId(null)
            .amount(BigDecimal.valueOf(100.00))
            .build();

    Set<ConstraintViolation<TransferDTO>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(
        violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("toAccountId")));
  }

  @Test
  void whenAmountIsNull_thenValidationFails() {
    TransferDTO request =
        TransferDTO.builder()
            .transferId("some-id")
            .fromAccountId("1")
            .toAccountId("2")
            .amount(null)
            .build();

    Set<ConstraintViolation<TransferDTO>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }

  @Test
  void whenAmountIsLessThanMinimum_thenValidationFails() {
    TransferDTO request =
        TransferDTO.builder()
            .transferId("some-id")
            .fromAccountId("1")
            .toAccountId("2")
            .amount(BigDecimal.valueOf(0.001)) // less than 0.01
            .build();

    Set<ConstraintViolation<TransferDTO>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }
}
