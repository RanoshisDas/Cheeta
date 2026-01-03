package com.ranoshisdas.app.cheeta.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class InvoiceSettings {

    private static final String PREFS_NAME = "InvoiceSettings";

    // Keys
    private static final String KEY_BUSINESS_NAME = "business_name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_GSTIN = "gstin";
    private static final String KEY_CGST_RATE = "cgst_rate";
    private static final String KEY_SGST_RATE = "sgst_rate";
    private static final String KEY_SETTINGS_COMPLETED = "settings_completed";

    // Default values
    private static final String DEFAULT_BUSINESS_NAME = "Your Business Name";
    private static final String DEFAULT_ADDRESS = "";
    private static final String DEFAULT_PHONE = "";
    private static final String DEFAULT_EMAIL = "";
    private static final String DEFAULT_GSTIN = "";
    private static final float DEFAULT_GST_RATE = 9.0f;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ========== Getters ==========

    public static String getBusinessName(Context context) {
        return getPrefs(context).getString(KEY_BUSINESS_NAME, DEFAULT_BUSINESS_NAME);
    }

    public static String getAddress(Context context) {
        return getPrefs(context).getString(KEY_ADDRESS, DEFAULT_ADDRESS);
    }

    public static String getPhone(Context context) {
        return getPrefs(context).getString(KEY_PHONE, DEFAULT_PHONE);
    }

    public static String getEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, DEFAULT_EMAIL);
    }

    public static String getGSTIN(Context context) {
        return getPrefs(context).getString(KEY_GSTIN, DEFAULT_GSTIN);
    }

    public static float getCGSTRate(Context context) {
        return getPrefs(context).getFloat(KEY_CGST_RATE, DEFAULT_GST_RATE);
    }

    public static float getSGSTRate(Context context) {
        return getPrefs(context).getFloat(KEY_SGST_RATE, DEFAULT_GST_RATE);
    }

    public static float getTotalGSTRate(Context context) {
        return getCGSTRate(context) + getSGSTRate(context);
    }

    public static boolean isSettingsCompleted(Context context) {
        return getPrefs(context).getBoolean(KEY_SETTINGS_COMPLETED, false);
    }

    // ========== Setters ==========

    public static void setBusinessName(Context context, String name) {
        getPrefs(context).edit().putString(KEY_BUSINESS_NAME, name.trim()).apply();
    }

    public static void setAddress(Context context, String address) {
        getPrefs(context).edit().putString(KEY_ADDRESS, address.trim()).apply();
    }

    public static void setPhone(Context context, String phone) {
        getPrefs(context).edit().putString(KEY_PHONE, phone.trim()).apply();
    }

    public static void setEmail(Context context, String email) {
        getPrefs(context).edit().putString(KEY_EMAIL, email.trim()).apply();
    }

    public static void setGSTIN(Context context, String gstin) {
        getPrefs(context).edit().putString(KEY_GSTIN, gstin.trim().toUpperCase()).apply();
    }

    public static void setCGSTRate(Context context, float rate) {
        if (isValidGSTRate(rate)) {
            getPrefs(context).edit().putFloat(KEY_CGST_RATE, rate).apply();
        }
    }

    public static void setSGSTRate(Context context, float rate) {
        if (isValidGSTRate(rate)) {
            getPrefs(context).edit().putFloat(KEY_SGST_RATE, rate).apply();
        }
    }

    public static void setSettingsCompleted(Context context, boolean completed) {
        getPrefs(context).edit().putBoolean(KEY_SETTINGS_COMPLETED, completed).apply();
    }

    // ========== Bulk Save ==========

    public static void saveAllSettings(Context context, String businessName, String address,
                                       String phone, String email, String gstin,
                                       float cgstRate, float sgstRate) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(KEY_BUSINESS_NAME, businessName.trim());
        editor.putString(KEY_ADDRESS, address.trim());
        editor.putString(KEY_PHONE, phone.trim());
        editor.putString(KEY_EMAIL, email.trim());
        editor.putString(KEY_GSTIN, gstin.trim().toUpperCase());

        if (isValidGSTRate(cgstRate)) {
            editor.putFloat(KEY_CGST_RATE, cgstRate);
        }
        if (isValidGSTRate(sgstRate)) {
            editor.putFloat(KEY_SGST_RATE, sgstRate);
        }

        editor.putBoolean(KEY_SETTINGS_COMPLETED, true);
        editor.apply();
    }

    // ========== Validation ==========

    public static boolean isValidGSTRate(float rate) {
        return rate >= 0 && rate <= 28;
    }

    public static boolean isValidGSTIN(String gstin) {
        if (gstin == null || gstin.trim().isEmpty()) {
            return true; // GSTIN is optional
        }
        String trimmed = gstin.trim();
        return trimmed.length() == 15 && trimmed.matches("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}");
    }

    public static String validateBusinessName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Business name is required";
        }
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number is required";
        }
        String trimmed = phone.trim();
        if (trimmed.length() < 10) {
            return "Phone number must be at least 10 digits";
        }
        return null;
    }

    public static String validateGSTIN(String gstin) {
        if (gstin != null && !gstin.trim().isEmpty() && !isValidGSTIN(gstin)) {
            return "GSTIN must be 15 characters (format: 22AAAAA0000A1Z5)";
        }
        return null;
    }

    public static String validateGSTRate(float rate) {
        if (!isValidGSTRate(rate)) {
            return "GST rate must be between 0% and 28%";
        }
        return null;
    }

    // ========== Helper Methods ==========

    public static boolean hasMinimumSettings(Context context) {
        String businessName = getBusinessName(context);
        String phone = getPhone(context);
        return !businessName.equals(DEFAULT_BUSINESS_NAME) && !phone.isEmpty();
    }

    public static void clearAllSettings(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}