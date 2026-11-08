package ru.roadmap.currency.api;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.roadmap.currency.service.CollectionJobService;
import ru.roadmap.currency.service.RateQueryService;

@RestController
public class RateController {
    private final CollectionJobService jobs;
    private final RateQueryService rates;
    public RateController(CollectionJobService jobs, RateQueryService rates) { this.jobs=jobs; this.rates=rates; }

    @PostMapping("/api/rates/collect") @ResponseStatus(HttpStatus.ACCEPTED)
    public JobResponse collect(@Valid @RequestBody(required = false) CollectRequest request) { return jobs.submit(request == null ? null : request.currencies()); }
    @GetMapping("/api/rates/jobs/{id}") public JobResponse job(@PathVariable String id) { return jobs.get(id); }
    @GetMapping({"/answer", "/api/rates"})
    public List<RateResponse> answer(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
                                     @RequestParam(required=false) String currencies, @RequestParam(defaultValue="currency") String sort,
                                     @RequestParam(defaultValue="false") boolean desc) {
        Set<String> filter = currencies == null || currencies.isBlank() ? Set.of() : Arrays.stream(currencies.split(",")).map(s -> s.trim().toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        return rates.find(date == null ? LocalDate.now() : date, filter, sort, desc).stream().map(RateResponse::from).toList();
    }
    @GetMapping({"/answer/{currency}", "/api/rates/{currency}"})
    public RateResponse answerOne(@PathVariable String currency, @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) {
        return RateResponse.from(rates.findOne(currency, date == null ? LocalDate.now() : date));
    }
}
