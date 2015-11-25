package com.example.arthika.arthikahft;

public class OrderItem {

    private String orderid;
    private String fixid;
    private String tinterface;
    private String security;
    private int quantity;
    private double limitprice;
    private int pips;
    private String side;
    private String type;
    private double finishedprice;
    private int finishedquantity;
    private double commission;
    private String commcurrency;
    private String status;
    private String reason;

    public OrderItem(String orderid, String fixid, String tinterface, String security, int quantity, double limitprice, int pips, String side, String type, double finishedprice, int finishedquantity, double commission, String commcurrency, String status, String reason) {
        this.orderid = orderid;
        this.fixid = fixid;
        this.tinterface = tinterface;
        this.security = security;
        this.quantity = quantity;
        this.limitprice = limitprice;
        this.pips = pips;
        this.side = side;
        this.type = type;
        this.finishedprice = finishedprice;
        this.finishedquantity = finishedquantity;
        this.commission = commission;
        this.commcurrency = commcurrency;
        this.status = status;
        this.reason = reason;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getFixid() {
        return fixid;
    }

    public void setFixid(String fixid) {
        this.fixid = fixid;
    }

    public String getTinterface() {
        return tinterface;
    }

    public void setTinterface(String tinterface) {
        this.tinterface = tinterface;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLimitprice() {
        return limitprice;
    }

    public void setLimitprice(double limitprice) {
        this.limitprice = limitprice;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFinishedprice() {
        return finishedprice;
    }

    public void setFinishedprice(double finishedprice) {
        this.finishedprice = finishedprice;
    }

    public int getFinishedquantity() {
        return finishedquantity;
    }

    public void setFinishedquantity(int finishedquantity) {
        this.finishedquantity = finishedquantity;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public String getCommcurrency() {
        return commcurrency;
    }

    public void setCommcurrency(String commcurrency) {
        this.commcurrency = commcurrency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
