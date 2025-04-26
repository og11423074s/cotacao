package org.br.mineradora.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.br.mineradora.dto.CurrencyPriceDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

@Path("/last")
@RegisterRestClient
@ApplicationScoped
@ClientHeaderParam(name = "x-api-key", value = "${currency-price-client.apikey}")
public interface CurrencyPriceClient {
    @GET
    @Path("/{pair}")
    CurrencyPriceDTO getPricePair(@PathParam("pair") String pair);
}
