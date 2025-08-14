package za.co.sanlam.transferservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import za.co.sanlam.transferservice.dto.TransferRequest;
import za.co.sanlam.transferservice.service.TransferService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TransferService transferService;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/transfers";
    }

    private TransferRequest buildRequest(String suffix) {
        return TransferRequest.builder()
                .transferId("t-" + suffix)
                .fromAccountId("acc-from-" + suffix)
                .toAccountId("acc-to-" + suffix)
                .amount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void testCreateTransfer() {
        // Arrange
        TransferRequest request = buildRequest("1");
        when(transferService.createTransfer(any(TransferRequest.class)))
                .thenReturn("Transfer Successful");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransferRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), entity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transfer Successful", response.getBody());
    }

    @Test
    void testCreateBatch() {
        TransferRequest req1 = buildRequest("1");
        TransferRequest req2 = buildRequest("2");

        when(transferService.createBatch(any())).thenReturn(List.of("OK1", "OK2"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<TransferRequest>> entity = new HttpEntity<>(List.of(req1, req2), headers);

        ResponseEntity<List> response = restTemplate.postForEntity(getBaseUrl() + "/batch", entity, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("OK1"));
        assertTrue(response.getBody().contains("OK2"));
    }

    @Test
    void testGetTransferStatusById() {
        String transferId = UUID.randomUUID().toString();
        when(transferService.getStatusByTransferId(transferId)).thenReturn("COMPLETED");

        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/" + transferId, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", response.getBody());
    }
}
