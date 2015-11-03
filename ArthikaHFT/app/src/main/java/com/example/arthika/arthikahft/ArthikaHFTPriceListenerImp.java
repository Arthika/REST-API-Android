package com.example.arthika.arthikahft;

import java.text.ParseException;
import java.util.List;

/**
 * Created by Jaime on 22/09/2015.
 */
class ArthikaHFTPriceListenerImp implements ArthikaHFTPriceListener {

    @Override
    public void timestampEvent(String timestamp) {
        //System.out.println("Response timestamp: " + timestamp + " Contents:");
        MainActivity.updateTime =  timestamp;
    }

    @Override
    public void heartbeatEvent() {
        //System.out.println("Heartbeat!");
    }

    @Override
    public void messageEvent(String message) {
        System.out.println("Message from server: " + message);
    }

    @Override
    public void priceEvent(List<ArthikaHFT.priceTick> priceTickList) {
        String[] prices = new String[MainActivity.PRICE_COLUMNS * (MainActivity.secs.size() + 1)];
        Double[] pricesArray = new Double[MainActivity.PRICE_COLUMNS * (MainActivity.secs.size() + 1)];
        for (int i=0; i<MainActivity.secs.size(); i++){
            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = Utils.doubleToString(0);
            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = Utils.doubleToString(0);
            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = 0.0;
            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = 0.0;
        }
        for (ArthikaHFT.priceTick tick : priceTickList){
            //System.out.println("Security: " + tick.security + " Price: " + tick.price + " Side: " + tick.side + " Liquidity: " + tick.liquidity);
            for (int i=0; i<MainActivity.secs.size(); i++){
                if (tick.security.equals(MainActivity.secs.get(i))){
                    if (tick.side.equals("ask")){
                        double bestask = pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 1];
                        if ((bestask<=0 || bestask>tick.price) && tick.price>0){
                            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = tick.price;
                            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = Utils.doubleToString(tick.price, tick.pips);
                        }
                    }
                    if (tick.side.equals("bid")){
                        double bestbid = pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 2];
                        if (bestbid<=0 || bestbid<tick.price){
                            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = tick.price;
                            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = Utils.doubleToString(tick.price, tick.pips);
                        }
                    }
                }
            }
        }
        synchronized(MainActivity.prices) {
            for (int i = 0; i < MainActivity.secs.size(); i++) {
                MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1];
                MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2];
            }
        }
        if (PricePop.securitySelected!=null && PricePop.asklist!=null && PricePop.bidlist!=null){
            for (int i=0; i<MainActivity.secs.size(); i++){
                if (MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS].equals(PricePop.securitySelected)){
                    try {
                        PricePop.intervallist.add(MainActivity.updateTime);
                        PricePop.asklist.add(Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1]));
                        PricePop.bidlist.add(Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void accountingEvent(ArthikaHFT.accountingTick accountingTick) {
        synchronized(MainActivity.accountingArray) {
            if (EquityPop.equitystrategylist !=null) {
                EquityPop.equitystrategylist.add(accountingTick.strategyPL);
                EquityPop.equitypoollist.add(accountingTick.totalequity);
                EquityPop.equityintervallist.add(MainActivity.updateTime);
                if (EquityPop.equityintervallist.size()> EquityPop.EQUITY_MAX_VALUES){
                    synchronized(EquityPop.equitystrategylist){
                        for (int i=0; i<10; i++){
                            EquityPop.equitystrategylist.remove(0);
                        }
                    }
                    synchronized(EquityPop.equitypoollist){
                        for (int i=0; i<10; i++){
                            EquityPop.equitypoollist.remove(0);
                        }
                    }
                    synchronized(EquityPop.equityintervallist){
                        for (int i=0; i<10; i++){
                            EquityPop.equityintervallist.remove(0);
                        }
                        EquityPop.timeIni="";
                    }
                }
            }
            MainActivity.accountingArray[0]=Utils.doubleToString(accountingTick.strategyPL);
            MainActivity.accountingArray[1]=Utils.doubleToString(accountingTick.totalequity);
            MainActivity.accountingArray[2]=Utils.doubleToString(accountingTick.usedmargin);
            MainActivity.accountingArray[3]=Utils.doubleToString(accountingTick.freemargin);
            MainActivity.accountingChanged = true;
        }
    }

    @Override
    public void assetPositionEvent(List<ArthikaHFT.assetPositionTick> assetPositionTickList) {
        synchronized(MainActivity.assetArray) {
            for (ArthikaHFT.assetPositionTick tick : assetPositionTickList) {
                System.out.println("Asset: " + tick.asset + " Account: " + tick.account + " Exposure: " + tick.exposure + " Risk: " + tick.totalrisk);
                String asset = tick.asset;
                String account = tick.account;
                if (account.equals("<AGGREGATED>")){
                    account = "ALL";
                }
                else{
                    continue;
                }
                boolean found = false;
                for (int i = 0; i < MainActivity.assetArray.size(); i = i + MainActivity.ASSET_COLUMNS) {
                    if (asset.equals(MainActivity.assetArray.get(i)) && account.equals(MainActivity.assetArray.get(i+1))) {
                        MainActivity.assetArray.set(i + 2, Utils.doubleToString(tick.exposure));
                        MainActivity.assetArray.set(i + 3, Utils.doubleToString(tick.totalrisk));
                        System.out.println("Asset Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MainActivity.assetArray.add(asset);
                    MainActivity.assetArray.add(account);
                    MainActivity.assetArray.add(Utils.doubleToString(tick.exposure));
                    MainActivity.assetArray.add(Utils.doubleToString(tick.totalrisk));
                    System.out.println("Asset Added");
                }
            }
            MainActivity.assetChanged = true;
        }
    }

    @Override
    public void securityPositionEvent(List<ArthikaHFT.securityPositionTick> securityPositionTickList) {
        synchronized(MainActivity.positionArray) {
            for (ArthikaHFT.securityPositionTick tick : securityPositionTickList) {
                System.out.println("Security: " + tick.security + " Account: " + tick.account + " Exposure: " + tick.exposure + " Price: " + tick.price + " Pips: " + tick.pips);
                String security = tick.security;
                String account = tick.account;
                if (account.equals("<AGGREGATED>")){
                    account = "ALL";
                }
                else{
                    continue;
                }
                boolean found = false;
                for (int i = 0; i < MainActivity.positionArray.size(); i = i + MainActivity.POSITION_COLUMNS) {
                    if (security.equals(MainActivity.positionArray.get(i)) && account.equals(MainActivity.positionArray.get(i+1))) {
                        MainActivity.positionArray.set(i + 2, Utils.doubleToString(tick.exposure));
                        MainActivity.positionArray.set(i + 3, tick.side);
                        MainActivity.positionArray.set(i + 4, Utils.doubleToString(tick.price, tick.pips));
                        System.out.println("Position Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MainActivity.positionArray.add(security);
                    MainActivity.positionArray.add(account);
                    MainActivity.positionArray.add(Utils.doubleToString(tick.exposure));
                    MainActivity.positionArray.add(tick.side);
                    MainActivity.positionArray.add(Utils.doubleToString(tick.price, tick.pips));
                    System.out.println("Position Added");
                }
            }
            MainActivity.positionChanged = true;
        }
    }

    @Override
    public void positionHeartbeatEvent(ArthikaHFT.positionHeartbeat positionHeartbeatList) {
        /*
        System.out.print("Asset: " );
        for (int i=0; i<positionHeartbeatList.asset.size(); i++){
            System.out.print(positionHeartbeatList.asset.get(i));
            if (i<positionHeartbeatList.asset.size()-1){
                System.out.print(",");
            }
        }
        System.out.print(" Security: " );
        for (int i=0; i<positionHeartbeatList.security.size(); i++){
            System.out.print(positionHeartbeatList.security.get(i));
            if (i<positionHeartbeatList.security.size()-1){
                System.out.print(", ");
            }
        }
        System.out.print(" Account: " );
        for (int i=0; i<positionHeartbeatList.account.size(); i++){
            System.out.print(positionHeartbeatList.account.get(i));
            if (i<positionHeartbeatList.account.size()-1){
                System.out.print(",");
            }
        }
        System.out.println();
        */
    }

    @Override
    public void orderEvent(List<ArthikaHFT.orderTick> orderTickList) {
        for (ArthikaHFT.orderTick tick : orderTickList){
            System.out.println("TempId: " + tick.tempid + " OrderId: " + tick.orderid + " FixId: " + tick.fixid + " Security: " + tick.security + " Account: " + tick.account + " Quantity: " + tick.quantity + " Type: " + tick.type + " Side: " + tick.side + " Status: " + tick.status);
            String orderid = tick.orderid;
            if ("in flux".equals(tick.status)) {
                continue;
            }
            if ("pending".equals(tick.status)) {
                synchronized(MainActivity.pendingOrderArray) {
                    boolean found = false;
                    for (int i = 0; i < MainActivity.pendingOrderArray.size(); i = i + MainActivity.PENDINGORDER_COLUMNS) {
                        if (orderid.equals(MainActivity.pendingOrderArray.get(i))) {
                            MainActivity.pendingOrderArray.set(i + 1, tick.fixid);
                            MainActivity.pendingOrderArray.set(i + 2, tick.security);
                            MainActivity.pendingOrderArray.set(i + 3, String.valueOf(tick.quantity));
                            MainActivity.pendingOrderArray.set(i + 4, tick.side);
                            MainActivity.pendingOrderArray.set(i + 5, Utils.doubleToString(tick.limitprice, tick.pips));
                            System.out.println("Pending Order Modified");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        MainActivity.pendingOrderArray.add(0, orderid);
                        MainActivity.pendingOrderArray.add(1, tick.fixid);
                        MainActivity.pendingOrderArray.add(2, tick.security);
                        MainActivity.pendingOrderArray.add(3, String.valueOf(tick.quantity));
                        MainActivity.pendingOrderArray.add(4, tick.side);
                        MainActivity.pendingOrderArray.add(5, Utils.doubleToString(tick.limitprice, tick.pips));
                        MainActivity.pendingOrderArray.add(6, "Modify");
                        MainActivity.pendingOrderArray.add(7, "Cancel");
                        System.out.println("Pending Order Added");
                    }
                    MainActivity.pendingOrderChanged = true;
                }
            }
            else{
                synchronized(MainActivity.pendingOrderArray) {
                    for (int i = 0; i < MainActivity.pendingOrderArray.size(); i = i + MainActivity.PENDINGORDER_COLUMNS) {
                        if (orderid.equals(MainActivity.pendingOrderArray.get(i))) {
                            for (int j = 0; j < MainActivity.PENDINGORDER_COLUMNS; j++) {
                                MainActivity.pendingOrderArray.remove(i);
                            }
                            MainActivity.pendingOrderChanged = true;
                            System.out.println("Pending Order Deleted");
                        }
                    }
                }
                boolean found = false;
                synchronized(MainActivity.closedOrderArray) {
                    for (int i = 0; i < MainActivity.closedOrderArray.size(); i = i + MainActivity.CLOSEDORDER_COLUMNS) {
                        if (orderid.equals(MainActivity.closedOrderArray.get(i))) {
                            MainActivity.closedOrderArray.set(i + 1, tick.security);
                            MainActivity.closedOrderArray.set(i + 2, String.valueOf(tick.finishedquantity));
                            MainActivity.closedOrderArray.set(i + 3, tick.side);
                            MainActivity.closedOrderArray.set(i + 4, Utils.doubleToString(tick.finishedprice, tick.pips));
                            MainActivity.closedOrderArray.set(i + 5, tick.status);
                            System.out.println("Order Modified");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        MainActivity.closedOrderArray.add(0, orderid);
                        MainActivity.closedOrderArray.add(1, tick.security);
                        MainActivity.closedOrderArray.add(2, String.valueOf(tick.finishedquantity));
                        MainActivity.closedOrderArray.add(3, tick.side);
                        MainActivity.closedOrderArray.add(4, Utils.doubleToString(tick.finishedprice, tick.pips));
                        MainActivity.closedOrderArray.add(5, tick.status);
                        System.out.println("Order Added");
                    }
                    MainActivity.closedOrderChanged = true;
                }
            }
        }
    }

    @Override
    public void orderHeartbeatEvent(ArthikaHFT.orderHeartbeat orderHeartbeat) {
        /*
        System.out.print("Security: " );
        for (int i=0; i<orderHeartbeat.security.size(); i++){
            System.out.print(orderHeartbeat.security.get(i));
            if (i<orderHeartbeat.security.size()-1){
                System.out.print(", ");
            }
        }
        System.out.print(" Interface: " );
        for (int i=0; i<orderHeartbeat.tinterface.size(); i++){
            System.out.print(orderHeartbeat.tinterface.get(i));
            if (i<orderHeartbeat.tinterface.size()-1){
                System.out.print(",");
            }
        }
        System.out.println();
        */
    }

}
