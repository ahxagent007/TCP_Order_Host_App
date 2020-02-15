package com.dexian.tcporderhost;

import java.util.ArrayList;
import java.util.List;

public class InfoData {

    private List<OrderList> orderList;
    private List<ItemList> itemList;

    public InfoData() {
        orderList = new ArrayList<OrderList>();
        itemList = new ArrayList<ItemList>();

    }

    public InfoData(List<OrderList> orderList, List<ItemList> itemList) {

        this.orderList = orderList;
        this.itemList = itemList;
    }

    public void AddOrder(String itemName, int itemQuantity, int tableNo){
        orderList.add(new OrderList(itemName, itemQuantity, tableNo));
    }

    public void AddItem(String itemName, int itemPrice){
        itemList.add(new ItemList(itemName, itemPrice));
    }



    public List<OrderList> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderList> orderList) {
        this.orderList = orderList;
    }

    public List<ItemList> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemList> itemList) {
        this.itemList = itemList;
    }
}
