package com.haulmont.projectplanning.costestimation.sharepoint;

import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.BaseItem;
import com.microsoft.graph.requests.GraphServiceClient;
import io.vavr.control.Try;
import okhttp3.Request;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.aad.msal4j.ClientCredentialFactory.createFromSecret;

@Component
public class SharepointFileApiAdapter {

    private GraphServiceClient<Request> graphClient;

    private SharepointClientProperties sharepointClientProperties;

    public SharepointFileApiAdapter(SharepointClientProperties sharepointClientProperties) {
        this.sharepointClientProperties = sharepointClientProperties;
    }


    @PostConstruct
    void init() {

        var app = Try.of(() -> ConfidentialClientApplication
                .builder(sharepointClientProperties.getClientId(),
                        createFromSecret(sharepointClientProperties.getClientSecret()))
                .authority(sharepointClientProperties.getAuthority())
                .build()).getOrElseThrow(e -> new RuntimeException(e));

        var clientCredentialParam = ClientCredentialParameters.builder(
                        Collections.singleton(sharepointClientProperties.getScope()))
                .build();

        //noinspection NullableProblems
        var iAuthenticationProvider = new IAuthenticationProvider() {

            @Override
            public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
                return app.acquireToken(clientCredentialParam)
                        .thenApply(IAuthenticationResult::accessToken);
            }
        };

        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(iAuthenticationProvider)
                .buildClient();

    }
    public BaseItem requestItemBySharedLink(String link) {
        return graphClient.shares(prepareShareLink(link))
                .driveItem().buildRequest().get();
    }

    public InputStream requestContentBySharedLink(String link) {
        return graphClient.shares(prepareShareLink(link))
                .driveItem().content().buildRequest().get();
    }

    private String prepareShareLink(String link) {
       return "u!" + Base64.getEncoder().withoutPadding().encodeToString(link.getBytes())
               .replace("/", "_").replace("+", "-");
    }
}
