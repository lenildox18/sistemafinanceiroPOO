package service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyServiceTest {

    @Test
    public void testGetRateCache() throws Exception {
        CurrencyService svc = new CurrencyService();
        BigDecimal r1 = svc.getRate("USD", "BRL");
        assertNotNull(r1);
        BigDecimal r2 = svc.getRate("USD", "BRL");
        assertEquals(0, r1.compareTo(r2)); // dentro do cache, deve ser igual
    }
}