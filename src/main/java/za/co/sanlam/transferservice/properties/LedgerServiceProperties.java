package za.co.sanlam.transferservice.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ledger.service.endpoint")
public class LedgerServiceProperties {
  private String baseUrl;
  private String path;
}
