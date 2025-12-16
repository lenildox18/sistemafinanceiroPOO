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
 * Classe responsável por chamadas HTTP à API de câmbio (exchangerate.host).
 * Faz cache simples por 10 minutos. Em caso de falha, permite fallback.
 */
public class CurrencyService {
    private static final String API_BASE = "https://api.exchangerate.host/convert";
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
        if (e != null && Instant.now().minus(ttl).isBefore(e.timestamp)) {
            return e.rate;
        }
        BigDecimal rate = fetchRateFromApi(from, to);
        cache.put(key, new CacheEntry(rate, Instant.now()));
        return rate;
    }

    private BigDecimal fetchRateFromApi(String from, String to) throws IOException, InterruptedException {
        String uri = API_BASE + "?from=" + from + "&to=" + to;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new IOException("Falha ao consultar API: status " + resp.statusCode());
        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
        if (!json.has("result")) throw new IOException("Resposta inválida da API");
        BigDecimal result = json.get("result").getAsBigDecimal();
        return result;
    }

    // utilitário para converter valor
    public BigDecimal convert(String from, String to, BigDecimal amount) throws IOException, InterruptedException {
        BigDecimal rate = getRate(from, to);
        return amount.multiply(rate);
    }
}