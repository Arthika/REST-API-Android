package com.example.arthika.arthikahft;

public class ClosePositionItem {

    private String security;
    private String account;
    private double quantity;
    private String side;

    public ClosePositionItem(String security, String account, double quantity, String side) {
        this.security = security;
        this.account = account;
        this.quantity = quantity;
        this.side = side;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

}
