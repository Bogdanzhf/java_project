package ru.roadmap.currency.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.roadmap.currency.domain.CurrencyRate;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    List<CurrencyRate> findByRateDate(LocalDate rateDate);
    List<CurrencyRate> findByCurrencyCodeInAndRateDate(List<String> codes, LocalDate rateDate);
    Optional<CurrencyRate> findByCurrencyCodeAndRateDate(String code, LocalDate date);
}
