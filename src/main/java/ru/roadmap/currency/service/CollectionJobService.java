package ru.roadmap.currency.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.roadmap.currency.api.JobResponse;

@Service
public class CollectionJobService {
    private static final Logger log = LoggerFactory.getLogger(CollectionJobService.class);
    private final BlockingQueue<Job> queue = new LinkedBlockingQueue<>(100);
    private final ConcurrentMap<String, Job> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final ScheduledExecutorService maintenanceScheduler;
    private final RateCollectionService collectionService;
    private final List<String> defaults;
    private volatile boolean running = true;
    private Thread dispatcher;

    public CollectionJobService(@Qualifier("parserExecutor") ExecutorService executor, @Qualifier("maintenanceScheduler") ScheduledExecutorService maintenanceScheduler, RateCollectionService collectionService, @Value("${parser.default-currencies}") String defaultCurrencies) {
        this.executor=executor; this.maintenanceScheduler=maintenanceScheduler; this.collectionService=collectionService;
        this.defaults=List.of(defaultCurrencies.split(",")).stream().map(s -> s.trim().toUpperCase(Locale.ROOT)).toList();
    }
    @PostConstruct void start() {
        dispatcher = new Thread(this::dispatchLoop, "collection-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
        maintenanceScheduler.scheduleAtFixedRate(() -> jobs.entrySet().removeIf(entry -> entry.getValue().createdAt.plusHours(24).isBefore(OffsetDateTime.now())), 1, 1, TimeUnit.HOURS);
    }
    public JobResponse submit(List<String> requested) {
        List<String> currencies = requested == null || requested.isEmpty() ? defaults : requested.stream().map(s -> s.trim().toUpperCase(Locale.ROOT)).distinct().toList();
        Job job = new Job(UUID.randomUUID().toString(), currencies);
        if (!queue.offer(job)) throw new RejectedExecutionException("Очередь сбора переполнена");
        jobs.put(job.id, job);
        return response(job, "Задание принято в очередь");
    }
    public JobResponse get(String id) { Job job=jobs.get(id); if(job==null) throw new java.util.NoSuchElementException("Задание не найдено: " + id); return response(job, job.message.get()); }
    private void dispatchLoop() {
        while (running) try {
            Job job = queue.take(); job.status.set("RUNNING");
            executor.submit(() -> { try { int saved=collectionService.collect(job.currencies); job.message.set("Сохранено или подтверждено записей: " + saved); job.status.set("COMPLETED"); } catch(Exception e) { log.warn("Job {} failed", job.id, e); job.message.set("Ошибка сбора: " + e.getMessage()); job.status.set("FAILED"); } });
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
    }
    private JobResponse response(Job job, String message) { return new JobResponse(job.id, job.status.get(), job.currencies, job.createdAt, message); }
    @PreDestroy void stop() { running=false; if(dispatcher!=null) dispatcher.interrupt(); }
    private static final class Job { final String id; final List<String> currencies; final OffsetDateTime createdAt=OffsetDateTime.now(); final AtomicReference<String> status=new AtomicReference<>("QUEUED"); final AtomicReference<String> message=new AtomicReference<>("В очереди"); Job(String id,List<String> currencies){this.id=id;this.currencies=currencies;} }
}
