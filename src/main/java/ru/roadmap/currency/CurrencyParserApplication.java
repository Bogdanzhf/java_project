package ru.roadmap.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// MVC server uses WebClient lazily in CbrXmlClient; avoid eagerly starting an HTTP event loop on application boot.
@SpringBootApplication(exclude = {WebClientAutoConfiguration.class, ClientHttpConnectorAutoConfiguration.class})
@EnableScheduling
public class CurrencyParserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CurrencyParserApplication.class, args);
    }
}
