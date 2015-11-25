package com.example.arthika.arthikahft;

public class AssetItem {

    private String asset;
    private String account;
    private double exposure;
    private double totalrisk;
    private double pl;

    public AssetItem(String security, String account, double exposure, double totalrisk, double pl) {
        this.asset = security;
        this.account = account;
        this.exposure = exposure;
        this.totalrisk = totalrisk;
        this.pl = pl;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
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

    public double getTotalrisk() {
        return totalrisk;
    }

    public void setTotalrisk(double totalrisk) {
        this.totalrisk = totalrisk;
    }

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }

}
