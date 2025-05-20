package uk.ac.hope.mcse.android.coursework.model;

public class PastOrder {
    public String date;
    public String time;
    public String items;
    public String deals;
    public String rewards;
    public String price;
    public String orderNumber;

    public PastOrder(String date, String time, String items, String deals, String rewards, String price, String orderNumber) {
        this.date = date;
        this.time = time;
        this.items = items;
        this.deals = deals;
        this.rewards = rewards;
        this.price = price;
        this.orderNumber = orderNumber;
    }
}
