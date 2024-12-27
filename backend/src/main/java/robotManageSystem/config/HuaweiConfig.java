package robotManageSystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "delegate")
public class HuaweiConfig {
    private String subAppId;
    private String domain;
    private String domainName;
    private String userName;
    private String password;
    private String endpoint;
    private String region;
    private String serviceType;
}