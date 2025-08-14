package za.co.sanlam.transferservice.validation;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(error ->
            errors.put(error.getPropertyPath().toString(), error.getMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

   /* @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(RecordNotFoundException ex) {

        return new ResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(ValidationException ex) {

        return new ResponseEntity(TransferStatus.FAILED.name(), HttpStatus.OK);
    }*/
}
