package org.br.mineradora.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.br.mineradora.client.CurrencyPriceClient;
import org.br.mineradora.dto.CurrencyPriceDTO;
import org.br.mineradora.dto.QuotationDTO;
import org.br.mineradora.entity.QuotationEntity;
import org.br.mineradora.message.KafkaEvents;
import org.br.mineradora.repository.QuotationRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@ApplicationScoped
public class QuotationService {

    Logger LOG = LoggerFactory.getLogger(QuotationService.class);

    public static final String USD_BRL = "USD-BRL";
    @Inject
    @RestClient
    CurrencyPriceClient currencyPriceClient;

    @Inject
    QuotationRepository quotationRepository;

    @Inject
    KafkaEvents kafkaEvents;

    public void getCurrencyPrice() {
        try {
            LOG.info("Starting getCurrencyPrice method");
            LOG.info("Calling API with pair: USD-BRL");

            CurrencyPriceDTO currencyPriceInfo = this.currencyPriceClient.getPricePair("USD-BRL");
            LOG.info("API Response received: {}", currencyPriceInfo);

            if (currencyPriceInfo != null) {
                LOG.info("CurrencyPriceDTO is not null");

                if (currencyPriceInfo.USDBRL != null) {
                    LOG.info("USDBRL data is not null: {}", currencyPriceInfo.USDBRL);
                    LOG.info("USDBRL fields:");
                    LOG.info("- code: {}", currencyPriceInfo.USDBRL.code);
                    LOG.info("- codein: {}", currencyPriceInfo.USDBRL.codein);
                    LOG.info("- name: {}", currencyPriceInfo.USDBRL.name);
                    LOG.info("- bid: {}", currencyPriceInfo.USDBRL.bid);
                    LOG.info("- pctChange: {}", currencyPriceInfo.USDBRL.pctChange);

                    if (updateCurrentInfoPrice(currencyPriceInfo)) {
                        LOG.info("Price updated, sending Kafka event");
                        this.kafkaEvents.sendNewKafkaEvent(QuotationDTO.builder()
                                .currencyPrice(new BigDecimal(currencyPriceInfo.USDBRL.bid))
                                .date(new Date())
                                .build());
                        LOG.info("Kafka event sent successfully");
                    } else {
                        LOG.info("Price not updated, no Kafka event sent");
                    }
                } else {
                    LOG.info("Error: USDBRL data is null");
                }
            } else {
                LOG.info("Error: API response is null");
            }
        } catch (Exception e) {
            LOG.error("Error calling currency API: {}", e.getMessage());
        }
    }

    private boolean updateCurrentInfoPrice(CurrencyPriceDTO currencyPriceInfo) {
        try {
            BigDecimal currentPrice = new BigDecimal(currencyPriceInfo.USDBRL.bid);
            boolean updatePrice = false;

            LOG.info("Current price from API: {}", currentPrice);

            List<QuotationEntity> quotationList = this.quotationRepository.findAll().list();

            if (quotationList.isEmpty()) {
                LOG.info("No previous quotations found, saving first quotation");
                this.saveQuotation(currencyPriceInfo);
                updatePrice = true;
            } else {
                QuotationEntity lastDollarPrice = quotationList.getLast();
                LOG.info("Last saved price: {}", lastDollarPrice.getCurrencyPrice());

                if (currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()) {
                    LOG.info("Price increased, updating quotation");
                    updatePrice = true;
                    this.saveQuotation(currencyPriceInfo);
                } else {
                    LOG.info("No price increase, not updating quotation");
                }
            }

            return updatePrice;
        } catch (Exception e) {
            LOG.error("Error in updateCurrentInfoPrice: {}", e.getMessage());
            return false;
        }
    }

    private void saveQuotation(CurrencyPriceDTO currencyPriceInfo) {
        try {
            QuotationEntity quotation = new QuotationEntity();

            quotation.setDate(new Date());
            quotation.setCurrencyPrice(new BigDecimal(currencyPriceInfo.USDBRL.bid));
            quotation.setPctChange(currencyPriceInfo.USDBRL.pctChange);
            quotation.setPair(USD_BRL);

            LOG.info("Saving quotation: {}", quotation);
            this.quotationRepository.persist(quotation);
            LOG.info("Quotation saved successfully");
        } catch (Exception e) {
            LOG.info("Error in saveQuotation: {}", e.getMessage());
        }
    }


}
