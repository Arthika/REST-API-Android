package com.example.arthika.arthikahft;

public class PositionItem {

    private String security;
    private String account;
    private double exposure;
    private double price;
    private int pips;
    private String side;
    private double pl;
    private String plcurrency;

    public PositionItem(String security, String account, double quantity, double price, int pips, String side, double pl, String plcurrency) {
        this.security = security;
        this.account = account;
        this.exposure = quantity;
        this.price = price;
        this.pips = pips;
        this.side = side;
        this.pl = pl;
        this.plcurrency = plcurrency;
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

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPips() {
        return pips;
    }

    public void setPips(int pips) {
        this.pips = pips;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }

    public String getPlcurrency() {
        return plcurrency;
    }

    public void setPlcurrency(String plcurrency) {
        this.plcurrency = plcurrency;
    }

}
