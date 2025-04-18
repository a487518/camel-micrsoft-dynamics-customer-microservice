package com.aadvika.s_sem_ec_customer.service;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aadvika.s_sem_ec_customer.config.Dynamics365Config;
import com.aadvika.s_sem_ec_customer.utils.QueryBuilder;

@Service
public class AccountService {

    private final Dynamics365Config dynamics365Config;
    
    @Value("${dynamics.fullQuery}")
    private String baseQuery;

    public AccountService(Dynamics365Config dynamics365Config) {
        this.dynamics365Config = dynamics365Config;
    }

    public void prepareExchange(Exchange exchange) {
        String accountId = exchange.getVariable("semId", String.class);
        String token = dynamics365Config.getAccessToken();
        String query = QueryBuilder.buildAccountQuery(baseQuery, accountId);

        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
        exchange.getIn().setHeader("Authorization", "Bearer " + token);
        exchange.getIn().setHeader("Accept", "application/json");
        exchange.getIn().setHeader(Exchange.HTTP_QUERY, query);
    }
}
