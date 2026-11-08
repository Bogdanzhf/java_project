package ru.roadmap.currency;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import ru.roadmap.currency.service.CbrXmlClient;

@SpringBootTest(properties = {
        "parser.refresh-delay-ms=86400000",
        "spring.datasource.url=jdbc:h2:mem:currencydb-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
})
@AutoConfigureMockMvc
class RateControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean CbrXmlClient cbrXmlClient;
    @Test void returnsSeededRatesAndSortsThem() throws Exception {
        mockMvc.perform(get("/answer").param("currencies", "USD,EUR").param("sort", "rate").param("desc", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].currency").value("EUR"));
    }
    @Test void acceptsCollectionJob() throws Exception {
        when(cbrXmlClient.loadRates()).thenReturn(List.of());
        mockMvc.perform(post("/api/rates/collect").contentType(MediaType.APPLICATION_JSON).content("{\"currencies\":[\"USD\",\"EUR\"]}"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.status").exists()).andExpect(jsonPath("$.currencies", hasSize(2)));
    }
}
