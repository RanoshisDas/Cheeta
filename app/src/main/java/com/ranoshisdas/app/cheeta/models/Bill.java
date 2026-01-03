package com.ranoshisdas.app.cheeta.models;

import java.io.Serializable;
import java.util.List;

public class Bill implements Serializable {
    public String billId;
    public Customer customer;
    public List<BillItem> items;

    // Amount breakdown
    public double subtotal;
    public double cgst;
    public double sgst;
    public double total;

    // GST rates used (for historical accuracy)
    public float cgstRate;
    public float sgstRate;

    // Business details at time of bill creation
    public BusinessDetails businessDetails;

    public long timestamp;

    public Bill() {}

    // Inner class for business details
    public static class BusinessDetails implements Serializable {
        public String name;
        public String address;
        public String phone;
        public String email;
        public String gstin;

        public BusinessDetails() {}

        public BusinessDetails(String name, String address, String phone, String email, String gstin) {
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.gstin = gstin;
        }
    }
}