package ru.roadmap.currency.service;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.roadmap.currency.domain.CurrencyRate;
import ru.roadmap.currency.repository.CurrencyRateRepository;

@Service
public class RateCollectionService {
    private static final Logger log = LoggerFactory.getLogger(RateCollectionService.class);
    private final CurrencyRateRepository repository;
    private final CbrXmlClient client;
    private final ReentrantLock writeLock = new ReentrantLock(true);
    public RateCollectionService(CurrencyRateRepository repository, CbrXmlClient client) { this.repository=repository; this.client=client; }

    @Transactional
    public int collect(List<String> requested) {
        writeLock.lock();
        try {
            List<CbrXmlClient.ParsedRate> parsed = client.loadRates();
            LocalDate previousDay = parsed.isEmpty() ? LocalDate.now().minusDays(1) : parsed.get(0).date().minusDays(1);
            Map<String, CbrXmlClient.ParsedRate> loadedPrevious;
            try {
                loadedPrevious = client.loadRates(previousDay).stream()
                        .collect(Collectors.toMap(CbrXmlClient.ParsedRate::code, Function.identity(), (left, right) -> left));
            } catch (RuntimeException e) {
                log.warn("Не удалось загрузить курсы ЦБ РФ за предыдущую дату {}; изменение курса будет недоступно", previousDay, e);
                loadedPrevious = Map.of();
            }
            final Map<String, CbrXmlClient.ParsedRate> previous = loadedPrevious;
            List<String> wanted = requested.stream().map(s -> s.toUpperCase(Locale.ROOT)).toList();
            int[] saved={0};
            parsed.stream().filter(rate -> wanted.contains(rate.code())).forEach(rate -> {
                OffsetDateTime now = OffsetDateTime.now();
                CbrXmlClient.ParsedRate previousRate = previous.get(rate.code());
                BigDecimal change = previousRate == null ? null : rate.value().subtract(previousRate.value());
                repository.findByCurrencyCodeAndRateDate(rate.code(), rate.date()).ifPresentOrElse(
                        existing -> existing.refresh(rate.name(), rate.value(), change, rate.nominal(), now, "CBR XML Daily"),
                        () -> repository.save(new CurrencyRate(rate.code(), rate.name(), rate.value(), change, rate.nominal(), rate.date(), now, "CBR XML Daily")));
                saved[0]++;
            });
            return saved[0];
        } finally { writeLock.unlock(); }
    }
    @Scheduled(fixedDelayString = "${parser.refresh-delay-ms}", initialDelay = 15000)
    public void scheduledRefresh() {
        try {
            collect(List.of("USD","EUR","CNY","GBP","JPY"));
        } catch (RuntimeException e) {
            log.warn("Не удалось автоматически обновить курсы валют с сайта ЦБ РФ", e);
        }
    }
}
