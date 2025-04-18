package com.aadvika.s_sem_ec_customer.utils;

public class QueryBuilder {
    public static String buildAccountQuery(String query, String accountId) {
        String baseQuery = query;
        String filterQuery = "&$filter=accountid eq '" + accountId + "'";
        String fullQuery = baseQuery + filterQuery;

        try {
            // return URLEncoder.encode(fullQuery, StandardCharsets.UTF_8.toString());
            return fullQuery;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode query", e);
        }
    }
}
