package com.ranoshisdas.app.cheeta.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for generating sequential bill numbers in format: MMM-YY-###
 * Example: JAN-26-001, FEB-26-002
 */
public class BillNumberGenerator {

    private static final String[] MONTH_NAMES = {
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

    /**
     * Get current month in YYYY-MM format for Firestore counter document
     * @return Current month string (e.g., "2026-01")
     */
    public static String getCurrentBillMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.US);
        return sdf.format(new Date());
    }

    /**
     * Format bill number from month and sequence
     * @param billMonth Month in YYYY-MM format (e.g., "2026-01")
     * @param sequence Sequential number (e.g., 1, 2, 3...)
     * @return Formatted bill number (e.g., "JAN-26-001")
     */
    public static String formatBillNumber(String billMonth, int sequence) {
        if (billMonth == null || billMonth.isEmpty()) {
            return "INVALID-00-000";
        }

        try {
            String[] parts = billMonth.split("-");
            if (parts.length != 2) {
                return "INVALID-00-000";
            }

            String year = parts[0].substring(2); // "2026" -> "26"
            String month = parts[1]; // "01"

            // Convert month number to 3-letter name
            int monthIndex = Integer.parseInt(month) - 1;
            if (monthIndex < 0 || monthIndex >= MONTH_NAMES.length) {
                return "INVALID-00-000";
            }
            String monthName = MONTH_NAMES[monthIndex];

            // Format: MMM-YY-###
            return String.format(Locale.US, "%s-%s-%03d", monthName, year, sequence);
        } catch (Exception e) {
            return "INVALID-00-000";
        }
    }

    /**
     * Parse bill month from bill number
     * @param billNumber Bill number (e.g., "JAN-26-001")
     * @return Bill month in YYYY-MM format or null if invalid
     */
    public static String parseBillMonth(String billNumber) {
        if (billNumber == null || billNumber.isEmpty()) {
            return null;
        }

        try {
            String[] parts = billNumber.split("-");
            if (parts.length != 3) {
                return null;
            }

            String monthName = parts[0];
            String year = "20" + parts[1]; // "26" -> "2026"

            // Find month number
            int monthNumber = -1;
            for (int i = 0; i < MONTH_NAMES.length; i++) {
                if (MONTH_NAMES[i].equals(monthName)) {
                    monthNumber = i + 1;
                    break;
                }
            }

            if (monthNumber == -1) {
                return null;
            }

            return String.format(Locale.US, "%s-%02d", year, monthNumber);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get sequence number from bill number
     * @param billNumber Bill number (e.g., "JAN-26-001")
     * @return Sequence number or -1 if invalid
     */
    public static int parseSequence(String billNumber) {
        if (billNumber == null || billNumber.isEmpty()) {
            return -1;
        }

        try {
            String[] parts = billNumber.split("-");
            if (parts.length != 3) {
                return -1;
            }

            return Integer.parseInt(parts[2]);
        } catch (Exception e) {
            return -1;
        }
    }
}