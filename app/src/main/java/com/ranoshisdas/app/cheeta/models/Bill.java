package com.ranoshisdas.app.cheeta.models;

import java.io.Serializable;
import java.util.List;

public class Bill implements Serializable {
    public String billId;
    public Customer customer;
    public List<BillItem> items;
    public double total;
    public long timestamp;

    public Bill() {}
}