package com.haulmont.projectplanning.costestimation.sharepoint;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "pcs.sharepoint.client")
public class SharepointClientProperties {

    private String clientId;
    private String clientSecret;
    private String authority;
    private String scope;

}
