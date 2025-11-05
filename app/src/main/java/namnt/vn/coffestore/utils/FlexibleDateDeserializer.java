package namnt.vn.coffestore.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Custom Date deserializer to handle various ISO 8601 date formats:
 * - With microseconds: "2025-11-04T17:27:33.6966419"
 * - With milliseconds: "2025-11-04T17:27:33.696"
 * - With timezone: "2025-11-04T17:27:33.696Z"
 * - Without fraction: "2025-11-04T17:27:33"
 */
public class FlexibleDateDeserializer implements JsonDeserializer<Date> {
    
    private static final String[] DATE_FORMATS = new String[] {
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",  // 7 digits microseconds (from API)
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",   // 6 digits
        "yyyy-MM-dd'T'HH:mm:ss.SSSSS",    // 5 digits
        "yyyy-MM-dd'T'HH:mm:ss.SSSS",     // 4 digits
        "yyyy-MM-dd'T'HH:mm:ss.SSS",      // 3 digits milliseconds
        "yyyy-MM-dd'T'HH:mm:ss.SS",       // 2 digits
        "yyyy-MM-dd'T'HH:mm:ss.S",        // 1 digit
        "yyyy-MM-dd'T'HH:mm:ss",           // No fraction
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", // With Z timezone
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", // With timezone offset
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",   // Standard ISO with Z
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",   // Standard ISO with offset
        "yyyy-MM-dd'T'HH:mm:ss'Z'",       // No fraction with Z
        "yyyy-MM-dd'T'HH:mm:ssXXX"        // No fraction with offset
    };

    @Override
    public Date deserialize(com.google.gson.JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateStr = json.getAsString();
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // Try each format
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Default to UTC if no timezone
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                // Try next format
                continue;
            }
        }

        // If all formats fail, try to normalize the date string
        try {
            // Remove microseconds beyond 3 digits (keep only milliseconds)
            String normalized = normalizeDateString(dateStr);
            if (normalized != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(normalized);
            }
        } catch (ParseException e) {
            // Fall through to throw exception
        }

        // If all parsing attempts fail, throw exception
        throw new JsonParseException("Unparseable date: " + dateStr);
    }

    /**
     * Normalize date string by truncating microseconds to milliseconds
     * Example: "2025-11-04T17:27:33.6966419" -> "2025-11-04T17:27:33.696"
     */
    private String normalizeDateString(String dateStr) {
        if (dateStr == null) return null;
        
        // Find the position of the decimal point
        int dotIndex = dateStr.indexOf('.');
        if (dotIndex == -1) {
            // No decimal point, try to add .000
            return dateStr + ".000";
        }
        
        // Find the position after the decimal (T or Z or end)
        int tIndex = dateStr.indexOf('T', dotIndex);
        int zIndex = dateStr.indexOf('Z', dotIndex);
        int plusIndex = dateStr.indexOf('+', dotIndex);
        int minusIndex = dateStr.indexOf('-', dotIndex + 1); // Skip first minus in date
        
        int endIndex = dateStr.length();
        if (zIndex != -1) endIndex = Math.min(endIndex, zIndex);
        if (plusIndex != -1) endIndex = Math.min(endIndex, plusIndex);
        if (minusIndex != -1 && minusIndex > dotIndex) endIndex = Math.min(endIndex, minusIndex);
        
        // Extract the fraction part
        String fraction = dateStr.substring(dotIndex + 1, endIndex);
        
        // Truncate to 3 digits (milliseconds)
        if (fraction.length() > 3) {
            fraction = fraction.substring(0, 3);
        } else if (fraction.length() < 3) {
            // Pad to 3 digits
            while (fraction.length() < 3) {
                fraction += "0";
            }
        }
        
        // Reconstruct the date string
        String prefix = dateStr.substring(0, dotIndex + 1);
        String suffix = dateStr.substring(endIndex);
        return prefix + fraction + suffix;
    }
}

