package ru.roadmap.currency.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import ru.roadmap.currency.domain.CurrencyRate;
import ru.roadmap.currency.repository.CurrencyRateRepository;

@Service
public class RateQueryService {
    private final CurrencyRateRepository repository;
    public RateQueryService(CurrencyRateRepository repository) { this.repository=repository; }
    public List<CurrencyRate> find(LocalDate date, Set<String> currencies, String sort, boolean descending) {
        Comparator<CurrencyRate> comparator = switch (sort == null ? "currency" : sort.toLowerCase(Locale.ROOT)) {
            case "rate" -> Comparator.comparing(CurrencyRate::getRateToRub);
            case "date" -> Comparator.comparing(CurrencyRate::getRateDate);
            default -> Comparator.comparing(CurrencyRate::getCurrencyCode);
        };
        if (descending) comparator=comparator.reversed();
        Comparator<CurrencyRate> finalComparator=comparator;
        return repository.findByRateDate(date).parallelStream()
                .filter(rate -> currencies == null || currencies.isEmpty() || currencies.contains(rate.getCurrencyCode()))
                .sorted(finalComparator).toList();
    }
    public CurrencyRate findOne(String currency, LocalDate date) {
        return repository.findByCurrencyCodeAndRateDate(currency.toUpperCase(Locale.ROOT), date)
                .orElseThrow(() -> new java.util.NoSuchElementException("Курс " + currency + " за " + date + " не найден"));
    }
}
