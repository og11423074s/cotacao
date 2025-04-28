package org.br.mineradora.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Provider
public class CurrencyPriceRequestFilter implements ClientRequestFilter {

    Logger LOG = LoggerFactory.getLogger(CurrencyPriceRequestFilter.class);

    @Inject
    @ConfigProperty(name = "currency-price-client.apikey")
    String apiKey;

    @Override
    public void filter(ClientRequestContext requestContext) {
        LOG.info("API key from config: {}", apiKey);
        LOG.info("Request URI: {}", requestContext.getUri());
        LOG.info("Request method: {}", requestContext.getMethod());
        LOG.info("Request headers: {}", requestContext.getHeaders());
    }
}
