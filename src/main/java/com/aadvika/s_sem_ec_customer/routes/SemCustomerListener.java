package com.aadvika.s_sem_ec_customer.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SemCustomerListener extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("jms:queue:{{solace.queue.name}}")
                .routeId("SOLACE_MESSAGE_SPLIT_AND_SEND")
                .setVariable("semId", simple("${jsonpath('$.PrimaryEntityId')}"))
                .log(LoggingLevel.INFO, "Value of semId: ${variable.semId}")
                .to("direct:getAccount");   

    }

}
