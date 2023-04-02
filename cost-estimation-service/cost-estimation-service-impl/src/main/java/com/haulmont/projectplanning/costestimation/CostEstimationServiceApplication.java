package com.haulmont.projectplanning.costestimation;

import com.haulmont.projectplanning.costestimation.sharepoint.SharepointClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
@EnableConfigurationProperties({ SharepointClientProperties.class })
public class CostEstimationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CostEstimationServiceApplication.class, args);
	}

}
