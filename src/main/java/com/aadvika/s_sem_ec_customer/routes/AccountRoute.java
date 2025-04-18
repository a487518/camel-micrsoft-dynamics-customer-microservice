package com.aadvika.s_sem_ec_customer.routes;


import org.apache.camel.LoggingLevel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.aadvika.s_sem_ec_customer.service.AccountService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountRoute extends RouteBuilder {

    private AccountService accountService;

    // // Constructor injection
    public AccountRoute(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void configure() throws Exception {
        configureErrorHandling();
        configureAccountRoutes();
    }

    private void configureErrorHandling() {
        errorHandler(deadLetterChannel("direct:error")
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .backOffMultiplier(2)
                .useExponentialBackOff());

        from("direct:error")
                .log(LoggingLevel.ERROR, "Error processing message: ${exception.message}")
                .log(LoggingLevel.DEBUG, "Stack trace: ${exception.stacktrace}")
                .to("log:com.example.dynamics.error?level=ERROR");
    }

    private void configureAccountRoutes() {

        from("direct:getAccount")
                .routeId("getAccountRoute")
                .log(LoggingLevel.INFO, "Route started")
                .process(accountService::prepareExchange) // Delegate logic to service
                .toD("{{dynamics.service-url}}")
                .choice()
                .when().jsonpath("$.value[?(@)]")
                .to("jslt:transformer/dynamics-to-business-partner.jslt?allowTemplateFromHeader=false")
                .log(LoggingLevel.INFO, "Account response from Dynamics: ${prettyBody}")
                .otherwise()
                .log(LoggingLevel.WARN, "No account found for the given AccountId ${header.semId}")
                .end()
                .log(LoggingLevel.INFO, "Rate Limit: ${header.x-ms-ratelimit-time-remaining-xrm-requests}")
                .log(LoggingLevel.INFO, "Account data retrieval process completed");
    }
}
