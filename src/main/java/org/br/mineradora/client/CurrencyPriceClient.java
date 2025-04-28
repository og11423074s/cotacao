package org.br.mineradora.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.br.mineradora.dto.CurrencyPriceDTO;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = "currency-api")
@ApplicationScoped
@RegisterProvider(CurrencyPriceRequestFilter.class)
@Path("/json/last")
public interface CurrencyPriceClient {

    @GET
    @Path("/{pair}")
    CurrencyPriceDTO getPricePair(@PathParam("pair") String pair);
}
