package ru.roadmap.currency.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConcurrencyConfig {
    @Bean(destroyMethod = "shutdown")
    ExecutorService parserExecutor() {
        return new ThreadPoolExecutor(3, 6, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), namedFactory("rate-parser-"), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(destroyMethod = "shutdown")
    ScheduledExecutorService maintenanceScheduler() {
        return new ScheduledThreadPoolExecutor(1, namedFactory("rate-maintenance-"));
    }

    private ThreadFactory namedFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> { Thread thread = new Thread(runnable, prefix + counter.incrementAndGet()); thread.setDaemon(true); return thread; };
    }
}
