package ru.roadmap.currency.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import ru.roadmap.currency.domain.CurrencyRate;

public record RateResponse(String currency, String name, BigDecimal rateToRub, BigDecimal dailyChange, Integer nominal, LocalDate date, OffsetDateTime collectedAt, String source) {
    public static RateResponse from(CurrencyRate rate) { return new RateResponse(rate.getCurrencyCode(), rate.getCurrencyName(), rate.getRateToRub(), rate.getDailyChange(), rate.getNominal(), rate.getRateDate(), rate.getCollectedAt(), rate.getSource()); }
}
