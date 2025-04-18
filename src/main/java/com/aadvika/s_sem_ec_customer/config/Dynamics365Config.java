package com.aadvika.s_sem_ec_customer.config;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.component.olingo4.Olingo4Component;
import org.apache.camel.component.olingo4.Olingo4Configuration;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class Dynamics365Config {

    @Value("${dynamics.service-url}")
    private String serviceUrl;

    @Value("${dynamics.tenant-id}")
    private String tenantId;

    @Value("${dynamics.client-id}")
    private String clientId;

    @Value("${dynamics.client-secret}")
    private String clientSecret;

    @Bean
    public Olingo4Configuration olingo4Configuration(){
        Olingo4Configuration config = new Olingo4Configuration();
        config.setServiceUri(serviceUrl);
        config.setContentType("application/json");

                // Create a custom HTTP client with auth interceptor
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
            String accessToken = getAccessToken();
            log.info("Access Token: {}", accessToken);
            request.addHeader("Authorization", "Bearer " + accessToken);
            request.addHeader("Accept", "application/json");
            request.addHeader("OData-MaxVersion", "4.0");
            request.addHeader("OData-Version", "4.0");
        });
        
        config.setHttpClientBuilder(httpClientBuilder);
        
        return config;
    }

    @Bean(name = "olingo4")
    public Olingo4Component olingo4Component(Olingo4Configuration config) {
        Olingo4Component component = new Olingo4Component();
        component.setConfiguration(config);
        return component;
    }

    public String getAccessToken() {
        try {
            IConfidentialClientApplication app = 
                ConfidentialClientApplication.builder(
                    clientId,
                    ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority("https://login.microsoftonline.com/" + tenantId)
                    .build();
                    
            ClientCredentialParameters parameters = 
                ClientCredentialParameters.builder(
                    Collections.singleton("https://ecsemtst.api.crm2.dynamics.com/.default"))
                    .build();
            
            CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
            IAuthenticationResult result = future.join();


            return result.accessToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire token for Dynamics 365", e);
        }
    }
    
}

