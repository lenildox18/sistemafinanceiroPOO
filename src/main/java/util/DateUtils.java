package util;

import java.time.LocalDate;

public class DateUtils {
    public static boolean isFuture(LocalDate d) {
        return d.isAfter(LocalDate.now());
    }
}