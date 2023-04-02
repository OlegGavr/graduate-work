package com.haulmont.projectplanning.costestimation.importer.excel;

import com.haulmont.projectplanning.costestimation.calc.Calculation;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.DriveSearchParameterSet;
import com.microsoft.graph.requests.GraphServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

//@SpringBootTest
class ExcelPlanImporterTest {

    @Autowired
    ObjectFactory<ExcelPlanImporter> hseExcelImporterFactory;

    @Autowired
    Calculation calculation;

    // test for data
    @Test
    void checkExcelImporting() {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport(resourceAsStream);

    }

    @Test
    void checkTimeForMiddleProject() {
        // given
        String resourceName = "FullHseExample.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        var project = hseExcelImporterFactory.getObject().doImport(resourceAsStream);

        calculation.calculate(project);
    }

    @Test
    void checkExcelAnotherTemplateImporting() {
        // given
        String resourceName = "AnotherTemplate.xlsx";

        ClassLoader classLoader = getClass().getClassLoader();
        var resourceAsStream = classLoader.getResourceAsStream(resourceName);

        // when
        hseExcelImporterFactory.getObject().doImport("62ce8e92bc15811dbc365e65", resourceAsStream);

    }

    @Test
    void sharePointIntegration() throws MalformedURLException, ExecutionException, InterruptedException {

        var clientId = "0b13cfca-94c8-44cf-8e3d-bceac342e171";
        var secret = "v6h8Q~PdzY1~fP.RbuaUdyJ5YNnkGosTOBc8zcYN";

        var app = ConfidentialClientApplication.builder(clientId,
                        ClientCredentialFactory.createFromSecret(secret))
                .authority("https://login.microsoftonline.com/ae1a2507-178f-4b3b-bc55-08b2f867f682/")
                .build();

        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
                        Collections.singleton("https://graph.microsoft.com/.default"))
                .build();

        var iAuthenticationResult = app.acquireToken(clientCredentialParam).get();


        System.out.printf("Token: %s%n", iAuthenticationResult.accessToken());

//        new TokenCredentialAuthProvider(app)
//        GraphServiceClient<Request> graphClient =
//                GraphServiceClient
//                        .builder()
//                        .authenticationProvider(app)
//                        .buildClient();
//        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
//                .clientId(clientId)
//                .clientSecret(secret)
//                .tenantId("ae1a2507-178f-4b3b-bc55-08b2f867f682")
//                .build();
//
//        TokenCredentialAuthProvider tokenCredentialAuthProvider =
//                new TokenCredentialAuthProvider(clientSecretCredential);
//
        var iAuthenticationProvider = new IAuthenticationProvider() {

            @Override
            public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
                return app
                        .acquireToken(clientCredentialParam)
                        .thenApply(IAuthenticationResult::accessToken);
            }
        };
        var graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(iAuthenticationProvider)
                        .buildClient();

//        System.out.printf("Token: %s%n", graphClient.);
//
//        var directory = graphClient.sites().buildRequest().get();
        var directory = graphClient.sites("fd72a133-ae85-4fd3-9415-9332ae0e7eb1")
                .drives("b!M6Fy_YWu00-UFZMyrg5-sTFwjgdSR-hGr7hRw2k8Zumq6bYPictXTYlCW0FCMPpl")
//                .items("01OOVBJNXDOTCFSSUBURFLW3ZTOJPYGOQV")
                .search(DriveSearchParameterSet.newBuilder().withQ("Кн").build())
//                .root()
//                .search(DriveItemSearchParameterSet.newBuilder().withQ("Кн").build())
//                .root().children()
                .buildRequest().get();

        var shares = graphClient.shares("u!aHR0cHM6Ly9oYXVsbW9udHMuc2hhcmVwb2ludC5jb20vOng6L2cvRVdxTmtaQWxvaDVJaW5sT2VYYW5JYlFCYXdZUkxnaHhEeG5JSjFYVVhXV1hwZz9lbWFpbD1pLmt1Y2htaW4lNDBoYXVsbW9udC5jb20mZT0wbUpES3Q")
                .buildRequest().get();


//        "A7C4587A-61DA-4AF7-85A8-26BE177A1542"
        ////        final User me = graphClient.me().buildRequest().get();
//
//        System.out.printf("Result: %s%n", directory.name);
        System.out.printf("Result: %s%n", directory.getCurrentPage().stream().map(i -> "%s: %s".formatted(i.folder, i.id)).toList());
        System.out.printf("Result: %s%n", shares.name);

    }
}
