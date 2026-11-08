package ru.roadmap.currency.api;

import jakarta.validation.constraints.Size;
import java.util.List;

public record CollectRequest(@Size(max = 5, message = "Можно запросить не более 5 валют") List<String> currencies) { }
