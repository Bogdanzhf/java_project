package ru.roadmap.currency.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.roadmap.currency.domain.CurrencyRate;
import ru.roadmap.currency.repository.CurrencyRateRepository;

@Configuration
public class SeedDataConfig {
    @Bean CommandLineRunner seedRates(CurrencyRateRepository repository) {
        return args -> {
            LocalDate today=LocalDate.now(); OffsetDateTime now=OffsetDateTime.now();
            Set<String> existingCodes = repository.findByRateDate(today).stream()
                    .map(CurrencyRate::getCurrencyCode)
                    .collect(Collectors.toSet());
            List<CurrencyRate> seedRates = List.of(
                new CurrencyRate("USD","Доллар США",new BigDecimal("97.5000"),new BigDecimal("0.1200"),1,today,now,"Демонстрационные данные"),
                new CurrencyRate("EUR","Евро",new BigDecimal("105.2000"),new BigDecimal("-0.0800"),1,today,now,"Демонстрационные данные"),
                new CurrencyRate("CNY","Китайский юань",new BigDecimal("13.4500"),new BigDecimal("0.0300"),1,today,now,"Демонстрационные данные"),
                new CurrencyRate("GBP","Фунт стерлингов",new BigDecimal("123.1000"),new BigDecimal("0.2500"),1,today,now,"Демонстрационные данные"),
                new CurrencyRate("JPY","Японская иена",new BigDecimal("63.8000"),new BigDecimal("-0.1500"),100,today,now,"Демонстрационные данные")
            );
            repository.saveAll(seedRates.stream()
                    .filter(rate -> !existingCodes.contains(rate.getCurrencyCode()))
                    .toList());
        };
    }
}
