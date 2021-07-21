package com.example.snugbites.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Order {

    private String orderId;
    private String orderTo;
    private String orderFrom;
    private String status;
    private String items;
    private int totalPrice;
    private int totalCount;
    @ServerTimestamp
    private Timestamp dateTime = null;

    public Order() {
    }

    public Order(String orderId, String orderTo, String orderFrom, String status, String items, int totalPrice, int totalCount) {
        this.orderId = orderId;
        this.orderTo = orderTo;
        this.orderFrom = orderFrom;
        this.status = status;
        this.items = items;
        this.totalPrice = totalPrice;
        this.totalCount = totalCount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getOrderFrom() {
        return orderFrom;
    }

    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }
}
