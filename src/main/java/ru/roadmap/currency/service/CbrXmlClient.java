package ru.roadmap.currency.service;

import java.math.BigDecimal;
import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class CbrXmlClient {
    private static final DateTimeFormatter REQUEST_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final String url;
    public CbrXmlClient(@Value("${parser.cbr-url}") String url) { this.url = url; }

    public List<ParsedRate> loadRates() {
        return loadRates(null);
    }

    public List<ParsedRate> loadRates(LocalDate date) {
        WebClient client = WebClient.builder().clientConnector(new JdkClientHttpConnector()).baseUrl(url).build();
        String xml = client.get().uri(builder -> date == null ? builder.build() : builder.queryParam("date_req", REQUEST_DATE.format(date)).build())
                .retrieve().bodyToMono(String.class).block(Duration.ofSeconds(8));
        if (xml == null || xml.isBlank()) throw new IllegalStateException("ЦБ РФ вернул пустой ответ");
        return parse(xml);
    }

    List<ParsedRate> parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            Element root = document.getDocumentElement();
            LocalDate date = LocalDate.parse(root.getAttribute("Date"), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            NodeList nodes = document.getElementsByTagName("Valute");
            List<ParsedRate> result = new ArrayList<>();
            for (int i=0; i<nodes.getLength(); i++) {
                Element item = (Element) nodes.item(i);
                result.add(new ParsedRate(text(item,"CharCode"), text(item,"Name"), new BigDecimal(text(item,"Value").replace(',', '.')), Integer.parseInt(text(item,"Nominal")), date));
            }
            return result;
        } catch (Exception e) { throw new IllegalStateException("Не удалось разобрать XML ЦБ РФ", e); }
    }
    private String text(Element parent, String tag) { return parent.getElementsByTagName(tag).item(0).getTextContent().trim(); }
    public record ParsedRate(String code, String name, BigDecimal value, int nominal, LocalDate date) { }
}
