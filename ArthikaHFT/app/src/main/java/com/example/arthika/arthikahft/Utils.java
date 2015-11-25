package com.example.arthika.arthikahft;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Utils {

    private static Locale locale = Locale.getDefault();
    private static NumberFormat format = NumberFormat.getInstance(locale);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static char decimalSeparator = ((DecimalFormat) format).getDecimalFormatSymbols().getDecimalSeparator();

    private static final NavigableMap<Double, Character> suffixesDouble = new TreeMap<>();
    static {
        suffixesDouble.put(1000.0, 'K');
        suffixesDouble.put(1000000.0, 'M');
        suffixesDouble.put(1000000000.0, 'G');
        //suffixes.put(1000000000000.0, 'T');
        //suffixes.put(1000000000000000.0, 'P');
        //suffixes.put(1000000000000000000.0, 'E');
    }

    private static final NavigableMap<Integer, Character> suffixesInt = new TreeMap<>();
    static {
        suffixesInt.put(1000, 'K');
        suffixesInt.put(1000000, 'M');
        suffixesInt.put(1000000000, 'G');
        //suffixes.put(1000000000000, 'T');
        //suffixes.put(1000000000000000, 'P');
        //suffixes.put(1000000000000000000, 'E');
    }

    public static int stringToInt(String value) throws ParseException {
        char suffix = value.charAt(value.length()-1);
        if (!Character.isDigit(suffix)){
            for (Map.Entry<Integer, Character> entry : suffixesInt.entrySet()) {
                if (entry.getValue().equals(suffix)) {
                    return stringToInt(value.substring(0, value.length() - 1)) * entry.getKey();
                }
            }
            return Integer.parseInt(value.substring(0, value.length() - 1));
        }
        else {
            return Integer.parseInt(value);
        }
    }

    public static String intToString(int value) {
        if (value < 0) return "-" + intToString(-value);
        if (value < 1000) return String.valueOf(value);

        Map.Entry<Integer, Character> e = suffixesInt.floorEntry(value);
        Integer divideBy = e.getKey();
        Character suffix = e.getValue();

        double truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? String.format(locale, "%.2f", truncated / 10d) + suffix : String.format(locale, "%.2f", truncated / 10) + suffix;
    }

    public static double stringToDouble(String value) throws ParseException {
        char suffix = value.charAt(value.length()-1);
        if (!Character.isDigit(suffix)){
            for (Map.Entry<Double, Character> entry : suffixesDouble.entrySet()) {
                if (entry.getValue().equals(suffix)) {
                    return stringToDouble(value.substring(0, value.length() - 2)) * entry.getKey();
                }
            }
        }
        return format.parse(value).doubleValue();
    }

    public static String doubleToString(double value) {
        if (value == Long.MIN_VALUE) return doubleToString(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + doubleToString(-value);
        if (value < 1000) return String.format(locale, "%.2f", value);

        Map.Entry<Double, Character> e = suffixesDouble.floorEntry(value);
        Double divideBy = e.getKey();
        Character suffix = e.getValue();

        double truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? String.format(locale, "%.2f",truncated / 10d) + suffix : String.format(locale, "%.2f", truncated / 10) + suffix;
    }

    public static String doubleToString(double value, int decimal) {
        return String.format(locale, "%." + decimal + "f", value);
    }

    public static String stringToStringNoDecimals(String value) throws ParseException {
        String res = value.substring(0,value.indexOf(decimalSeparator));
        char suffix = value.charAt(value.length()-1);
        if (!Character.isDigit(suffix)){
            res = res.concat(String.valueOf(suffix));
        }
        return res;
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
