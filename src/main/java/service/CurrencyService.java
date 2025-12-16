package service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Classe responsável por chamadas HTTP à API de câmbio (AwesomeAPI).
 * Faz cache simples por 10 minutos. Em caso de falha, lança exceção.
 */
public class CurrencyService {

    // URL atualizada para a AwesomeAPI (que é gratuita e não precisa de chave)
    private static final String API_BASE = "https://economia.awesomeapi.com.br/last/";

    private final HttpClient http;
    private final Map<String, CacheEntry> cache;
    private final Duration ttl = Duration.ofMinutes(10);

    public CurrencyService() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.cache = new ConcurrentHashMap<>();
    }

    public static class CacheEntry {
        public final BigDecimal rate;
        public final Instant timestamp;
        public CacheEntry(BigDecimal rate, Instant timestamp) {
            this.rate = rate;
            this.timestamp = timestamp;
        }
    }

    private String cacheKey(String from, String to) {
        return from + "_" + to;
    }

    public BigDecimal getRate(String from, String to) throws IOException, InterruptedException {
        String key = cacheKey(from, to);
        CacheEntry e = cache.get(key);

        // Verifica se existe no cache e se ainda é válido (menos de 10 min)
        if (e != null && Instant.now().minus(ttl).isBefore(e.timestamp)) {
            System.out.println("Recuperado do cache: " + key); // Log opcional para você ver funcionando
            return e.rate;
        }

        // Se não tiver no cache, busca na API
        BigDecimal rate = fetchRateFromApi(from, to);
        cache.put(key, new CacheEntry(rate, Instant.now()));
        return rate;
    }

    // MÉTODO ATUALIZADO PARA AWESOMEAPI
    private BigDecimal fetchRateFromApi(String from, String to) throws IOException, InterruptedException {
        // Monta a URL no padrão: .../last/USD-BRL
        String uri = API_BASE + from + "-" + to;

        // System.out.println("Consultando API: " + uri); // Descomente se precisar debugar

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new IOException("Falha na API (Status " + resp.statusCode() + "): " + resp.body());
        }

        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

        // A chave no JSON vem combinada, ex: "USDBRL"
        String jsonKey = from + to;

        if (!json.has(jsonKey)) {
            throw new IOException("Cotação não encontrada na resposta para: " + jsonKey);
        }

        // Pega o valor "bid" (compra)
        BigDecimal result = json.getAsJsonObject(jsonKey).get("bid").getAsBigDecimal();
        return result;
    }

    // Utilitário para converter valor
    public BigDecimal convert(String from, String to, BigDecimal amount) throws IOException, InterruptedException {
        BigDecimal rate = getRate(from, to);
        return amount.multiply(rate);
    }
}