package com.ranoshisdas.app.cheeta.models;

import java.util.List;

public class Bill {
    public String billId;
    public Customer customer;
    public List<Item> items;
    public double total;
    public long timestamp;

    public Bill() {}
}

