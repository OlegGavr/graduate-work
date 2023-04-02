package com.haulmont.projectplanning.gateway.api.config;

import com.haulmont.projectplanning.gateway.api.client.CostProjectApi;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import com.haulmont.projectplanning.gateway.api.invoker.ApiClient;

public class GatewayApiClientConfiguration {

    private ApiClient apiClient;

    public GatewayApiClientConfiguration(WebClient.Builder webClientBuilder) {
        var baseUrl = "http://gateway-service/";

        var webClient = webClientBuilder.baseUrl(baseUrl).build();
        var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(baseUrl);

        this.apiClient = apiClient;
    }

    @Bean
    CostProjectApi projectCostApi() {
        return new CostProjectApi(apiClient);
    }
//
//    @Bean
//    DepartmentControllerApi departmentControllerApi() {
//        return new DepartmentControllerApi(apiClient);
//    }
//
//    @Bean
//    OrganizationControllerApi organizationControllerApi() {
//        return new OrganizationControllerApi(apiClient);
//    }
}
