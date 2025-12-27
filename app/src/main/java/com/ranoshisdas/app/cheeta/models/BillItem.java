package com.ranoshisdas.app.cheeta.models;

import java.io.Serializable;

public class BillItem implements Serializable {
    public String id;
    public String name;
    public double price;
    public int quantity;
    public double subtotal;

    public BillItem() {}

    public BillItem(Item item, int quantity) {
        this.id = item.id;
        this.name = item.name;
        this.price = item.price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    public void updateSubtotal() {
        this.subtotal = price * quantity;
    }
}