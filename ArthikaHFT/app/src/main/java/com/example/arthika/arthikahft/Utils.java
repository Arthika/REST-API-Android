package com.example.arthika.arthikahft;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by Jaime on 29/10/2015.
 */
public class Utils {

    private static Locale locale = Locale.getDefault();
    private static NumberFormat format = NumberFormat.getInstance(locale);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final NavigableMap<Double, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1000.0, "K");
        suffixes.put(1000000.0, "M");
        suffixes.put(1000000000.0, "G");
        suffixes.put(1000000000000.0, "T");
        suffixes.put(1000000000000000.0, "P");
        suffixes.put(1000000000000000000.0, "E");
    }

    public static double stringToDouble(String value) throws ParseException {
        return format.parse(value).doubleValue();
    }

    public static String doubleToString(double value) {
        if (value == Long.MIN_VALUE) return doubleToString(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + doubleToString(-value);
        if (value < 1000) return String.format(locale, "%.2f", value);

        Map.Entry<Double, String> e = suffixes.floorEntry(value);
        Double divideBy = e.getKey();
        String suffix = e.getValue();

        double truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? String.format(locale, "%.2f",truncated / 10d) + suffix : String.format(locale, "%.2f", truncated / 10) + suffix;
    }

    public static String doubleToString(double value, int decimal) {
        return String.format(locale, "%." + decimal + "f", value);
    }

    public static String dateToString(long timelong) {
        Date date = new Date();
        date.setTime(timelong);
        return dateFormat.format(date);
    }

    public static String timeToString(long timelong) {
        Date date = new Date();
        date.setTime(timelong);
        return timeFormat.format(date);
    }

}
