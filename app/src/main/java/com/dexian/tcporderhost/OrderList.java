package com.dexian.tcporderhost;

public class OrderList {

    private String ItemName;
    private int ItemQuantity;
    private int TableNo;

    public OrderList() {
    }

    public OrderList(String itemName, int itemQuantity, int tableNo) {
        ItemName = itemName;
        ItemQuantity = itemQuantity;
        TableNo = tableNo;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public int getItemQuantity() {
        return ItemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        ItemQuantity = itemQuantity;
    }

    public int getTableNo() {
        return TableNo;
    }

    public void setTableNo(int tableNo) {
        TableNo = tableNo;
    }
}
