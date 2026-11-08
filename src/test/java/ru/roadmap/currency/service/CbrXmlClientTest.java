package ru.roadmap.currency.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CbrXmlClientTest {
    private final CbrXmlClient client = new CbrXmlClient("https://example.test");

    @Test
    void parsesCbrXmlWithRussianNameAndCommaDecimal() {
        String xml = """
                <?xml version=\"1.0\" encoding=\"windows-1251\"?>
                <ValCurs Date=\"08.11.2026\" name=\"Foreign Currency Market\">
                  <Valute ID=\"R01235\"><NumCode>840</NumCode><CharCode>USD</CharCode><Nominal>1</Nominal><Name>Доллар США</Name><Value>97,5000</Value></Valute>
                </ValCurs>
                """;

        List<CbrXmlClient.ParsedRate> result = client.parse(xml);

        assertThat(result).singleElement().satisfies(rate -> {
            assertThat(rate.code()).isEqualTo("USD");
            assertThat(rate.name()).isEqualTo("Доллар США");
            assertThat(rate.value()).isEqualByComparingTo(new BigDecimal("97.5000"));
            assertThat(rate.date()).isEqualTo(LocalDate.of(2026, 11, 8));
        });
    }
}
