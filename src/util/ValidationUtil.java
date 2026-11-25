package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input data.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    );

    private static final Pattern REGISTER_NO_PATTERN = Pattern.compile(
            "^[A-Za-z0-9\\-_/]{3,30}$"
    );

    /**
     * Checks if a string is null or whitespace-only.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates student or user full name.
     */
    public static boolean isValidName(String name) {
        if (isEmpty(name)) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100 && trimmed.matches("^[a-zA-Z\\s.'-]+$");
    }

    /**
     * Validates register number format.
     */
    public static boolean isValidRegisterNumber(String regNo) {
        if (isEmpty(regNo)) return false;
        return REGISTER_NO_PATTERN.matcher(regNo.trim()).matches();
    }

    /**
     * Validates email address format.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates minimum password criteria (at least 6 characters).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates and parses a date string in format YYYY-MM-DD.
     */
    public static java.sql.Date parseSqlDate(String dateStr) {
        if (isEmpty(dateStr)) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date parsed = sdf.parse(dateStr.trim());
            return new java.sql.Date(parsed.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Checks if the date string is in valid YYYY-MM-DD format.
     */
    public static boolean isValidDateFormat(String dateStr) {
        return parseSqlDate(dateStr) != null;
    }

    /**
     * Checks whether the date is not in the future.
     */
    public static boolean isNotFutureDate(java.sql.Date date) {
        if (date == null) return false;
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        return !date.after(today);
    }
}

