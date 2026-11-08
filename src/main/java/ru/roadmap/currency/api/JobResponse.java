package ru.roadmap.currency.api;

import java.time.OffsetDateTime;
import java.util.List;
public record JobResponse(String jobId, String status, List<String> currencies, OffsetDateTime createdAt, String message) { }
