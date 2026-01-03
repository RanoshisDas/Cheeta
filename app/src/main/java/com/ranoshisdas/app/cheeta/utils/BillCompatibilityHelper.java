package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import com.ranoshisdas.app.cheeta.models.Bill;

/**
 * Handles backward compatibility for bills created before Firebase integration.
 * Ensures all bills have the required data structure for PDF/Image generation.
 */
public class BillCompatibilityHelper {

    /**
     * Ensures bill has all required fields for PDF/Image generation.
     * Falls back to current settings if businessDetails is null (legacy bills).
     *
     * @param context Application context
     * @param bill Bill object to check
     * @return true if bill is compatible or was successfully made compatible, false otherwise
     */
    public static boolean ensureBillCompatibility(Context context, Bill bill) {
        if (bill == null) {
            return false;
        }

        // If bill already has business details, it's compatible
        if (bill.businessDetails != null) {
            // Validate that business details are not empty
            return isBusinessDetailsValid(bill.businessDetails);
        }

        // Legacy bill - check if current settings are configured
        if (!InvoiceSettings.hasMinimumSettings(context)) {
            return false; // Cannot create business details without settings
        }

        // Create business details from current settings
        bill.businessDetails = new Bill.BusinessDetails(
                InvoiceSettings.getBusinessName(context),
                InvoiceSettings.getAddress(context),
                InvoiceSettings.getPhone(context),
                InvoiceSettings.getEmail(context),
                InvoiceSettings.getGSTIN(context)
        );

        // If GST rates are missing, get from current settings
        if (bill.cgstRate == 0 && bill.sgstRate == 0) {
            bill.cgstRate = InvoiceSettings.getCGSTRate(context);
            bill.sgstRate = InvoiceSettings.getSGSTRate(context);
        }

        // If GST amounts are missing, calculate them
        if (bill.cgst == 0 && bill.sgst == 0) {
            calculateGSTAmounts(bill);
        }

        // If subtotal is missing, calculate from items or back-calculate from total
        if (bill.subtotal == 0) {
            calculateSubtotal(bill);
        }

        return true;
    }

    /**
     * Check if bill needs migration to new structure
     */
    public static boolean needsMigration(Bill bill) {
        return bill != null && bill.businessDetails == null;
    }

    /**
     * Validate that business details have minimum required information
     */
    private static boolean isBusinessDetailsValid(Bill.BusinessDetails details) {
        if (details == null) {
            return false;
        }
        // At minimum, we need business name and phone
        return details.name != null && !details.name.isEmpty() &&
                details.phone != null && !details.phone.isEmpty();
    }

    /**
     * Calculate GST amounts based on subtotal and rates
     */
    private static void calculateGSTAmounts(Bill bill) {
        if (bill.subtotal > 0) {
            bill.cgst = (bill.subtotal * bill.cgstRate) / 100;
            bill.sgst = (bill.subtotal * bill.sgstRate) / 100;

            // Update total if needed
            if (bill.total == 0) {
                bill.total = bill.subtotal + bill.cgst + bill.sgst;
            }
        }
    }

    /**
     * Calculate subtotal from items or back-calculate from total
     */
    private static void calculateSubtotal(Bill bill) {
        // Try to calculate from items first
        if (bill.items != null && !bill.items.isEmpty()) {
            double sum = 0;
            for (com.ranoshisdas.app.cheeta.models.BillItem item : bill.items) {
                sum += item.subtotal;
            }
            if (sum > 0) {
                bill.subtotal = sum;
                return;
            }
        }

        // Back-calculate from total if available
        if (bill.total > 0 && bill.cgstRate > 0 && bill.sgstRate > 0) {
            double totalGSTRate = bill.cgstRate + bill.sgstRate;
            bill.subtotal = bill.total / (1 + (totalGSTRate / 100));
            bill.cgst = (bill.subtotal * bill.cgstRate) / 100;
            bill.sgst = (bill.subtotal * bill.sgstRate) / 100;
        }
    }

    /**
     * Get a human-readable status message for bill compatibility
     */
    public static String getCompatibilityStatus(Context context, Bill bill) {
        if (bill == null) {
            return "Invalid bill";
        }

        if (bill.businessDetails != null && isBusinessDetailsValid(bill.businessDetails)) {
            return "Bill ready - all details present";
        }

        if (needsMigration(bill)) {
            if (InvoiceSettings.hasMinimumSettings(context)) {
                return "Legacy bill - will use current settings";
            } else {
                return "Legacy bill - settings required";
            }
        }

        return "Bill status unknown";
    }
}