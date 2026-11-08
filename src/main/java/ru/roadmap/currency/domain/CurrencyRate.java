package ru.roadmap.currency.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "currency_rates", uniqueConstraints = @UniqueConstraint(columnNames = {"currency_code", "rate_date"}))
public class CurrencyRate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="currency_code", nullable = false, length = 3) private String currencyCode;
    @Column(name="currency_name", nullable = false) private String currencyName;
    @Column(name="rate_to_rub", nullable = false, precision = 19, scale = 6) private BigDecimal rateToRub;
    @Column(name="daily_change", precision = 19, scale = 6) private BigDecimal dailyChange;
    @Column(nullable = false) private Integer nominal;
    @Column(name="rate_date", nullable = false) private LocalDate rateDate;
    @Column(name="collected_at", nullable = false) private OffsetDateTime collectedAt;
    @Column(nullable = false) private String source;
    protected CurrencyRate() { }
    public CurrencyRate(String code, String name, BigDecimal rate, BigDecimal change, Integer nominal, LocalDate date, OffsetDateTime collectedAt, String source) { this.currencyCode=code; this.currencyName=name; this.rateToRub=rate; this.dailyChange=change; this.nominal=nominal; this.rateDate=date; this.collectedAt=collectedAt; this.source=source; }
    public void refresh(String name, BigDecimal rate, BigDecimal change, Integer newNominal, OffsetDateTime time, String newSource) { this.currencyName=name; this.rateToRub=rate; this.dailyChange=change; this.nominal=newNominal; this.collectedAt=time; this.source=newSource; }
    public Long getId(){return id;} public String getCurrencyCode(){return currencyCode;} public String getCurrencyName(){return currencyName;} public BigDecimal getRateToRub(){return rateToRub;} public BigDecimal getDailyChange(){return dailyChange;} public Integer getNominal(){return nominal;} public LocalDate getRateDate(){return rateDate;} public OffsetDateTime getCollectedAt(){return collectedAt;} public String getSource(){return source;}
}
