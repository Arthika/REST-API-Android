package com.example.arthika.arthikahft;

import java.text.ParseException;
import java.util.List;

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
    public void priceEvent(List<ArthikaHFT.priceTick > priceTickList) {
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
                    if (tick.side.equals(ArthikaHFT.SIDE_ASK)){
                        double bestask = pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 1];
                        if ((bestask<=0 || bestask>tick.price) && tick.price>0){
                            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = tick.price;
                            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = Utils.doubleToString(tick.price, tick.pips);
                        }
                    }
                    if (tick.side.equals(ArthikaHFT.SIDE_BID)){
                        double bestbid = pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 2];
                        if (bestbid<=0 || bestbid<tick.price){
                            pricesArray[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = tick.price;
                            prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = Utils.doubleToString(tick.price, tick.pips);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < MainActivity.secs.size(); i++) {
            try {
                if (Utils.stringToDouble(prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1])>0 && Utils.stringToDouble(prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2])>0) {
                    MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1] = prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1];
                    MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2] = prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2];
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (PricePop.securitySelected!=null && PricePop.askList!=null && PricePop.bidList!=null){
            for (int i=0; i<MainActivity.secs.size(); i++){
                if (MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS].equals(PricePop.securitySelected)){
                    synchronized(PricePop.intervalList) {
                        try {
                            if (Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1])>0 && Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2])>0) {
                                PricePop.intervalList.add(MainActivity.updateTime);
                                PricePop.askList.add(Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 1]));
                                PricePop.bidList.add(Utils.stringToDouble(MainActivity.prices[(i + 1) * MainActivity.PRICE_COLUMNS + 2]));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void accountingEvent(ArthikaHFT.accountingTick accountingTick) {
        synchronized(MainActivity.accountingArray) {
            if (EquityPop.equityStrategyList !=null) {
                EquityPop.intervalList.add(MainActivity.updateTime);
                EquityPop.equityStrategyList.add(accountingTick.strategyPL);
                EquityPop.equityPoolList.add(accountingTick.totalequity);
                if (EquityPop.intervalList.size() > EquityPop.EQUITY_MAX_VALUES){
                    int clearValues = EquityPop.intervalList.size() - EquityPop.EQUITY_MAX_VALUES;
                    synchronized(EquityPop.equityStrategyList){
                        for (int i=0; i<clearValues; i++){
                            EquityPop.equityStrategyList.remove(0);
                        }
                    }
                    synchronized(EquityPop.equityPoolList){
                        for (int i=0; i<clearValues; i++){
                            EquityPop.equityPoolList.remove(0);
                        }
                    }
                    synchronized(EquityPop.intervalList){
                        for (int i=0; i<clearValues; i++){
                            EquityPop.intervalList.remove(0);
                        }
                        EquityPop.timeIni="";
                    }
                }
            }
            MainActivity.accountingArray[0]=Utils.doubleToString(accountingTick.strategyPL);
            MainActivity.accountingArray[1]=Utils.doubleToString(accountingTick.totalequity);
            MainActivity.accountingArray[2]=Utils.doubleToString(accountingTick.usedmargin);
            MainActivity.accountingArray[3]=Utils.doubleToString(accountingTick.freemargin);
            MainActivity.m2mcurrency = accountingTick.m2mcurrency;
            MainActivity.accountingChanged = true;
        }
    }

    @Override
    public void assetPositionEvent(List<ArthikaHFT.assetPositionTick> assetPositionTickList) {
        synchronized(MainActivity.assetItemList) {
            for (ArthikaHFT.assetPositionTick tick : assetPositionTickList) {
                //System.out.println("Asset: " + tick.asset + " Account: " + tick.account + " Exposure: " + tick.exposure + " Risk: " + tick.totalrisk);
                String asset = tick.asset;
                String account = tick.account;
                if (account.equals(MainActivity.AGREGATED)){
                    account = MainActivity.ALL;
                }
                boolean found = false;
                for (AssetItem assetItem : MainActivity.assetItemList) {
                    if (asset.equals(assetItem.getAsset()) && account.equals(assetItem.getAccount())) {
                        assetItem.setExposure(tick.exposure);
                        assetItem.setTotalrisk(tick.totalrisk);
                        assetItem.setPl(tick.pl);
                        //System.out.println("Asset Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    AssetItem assetItem = new AssetItem(asset,account,tick.exposure,tick.totalrisk,tick.pl);
                    MainActivity.assetItemList.add(assetItem);
                    System.out.println("Asset Added");
                }
            }
            MainActivity.assetChanged = true;
        }
    }

    @Override
    public void securityPositionEvent(List<ArthikaHFT.securityPositionTick> securityPositionTickList) {
        synchronized(MainActivity.positionItemList) {
            for (ArthikaHFT.securityPositionTick tick : securityPositionTickList) {
                //System.out.println("Security: " + tick.security + " Account: " + tick.account + " Exposure: " + tick.exposure + " Price: " + tick.price + " Pips: " + tick.pips);
                String security = tick.security;
                String account = tick.account;
                if (account.equals(MainActivity.AGREGATED)){
                    account = MainActivity.ALL;
                }
                boolean found = false;
                for (PositionItem positionItem : MainActivity.positionItemList) {
                    if (security.equals(positionItem.getSecurity()) && account.equals(positionItem.getAccount())) {
                        positionItem.setExposure(tick.exposure);
                        positionItem.setPrice(tick.price);
                        positionItem.setPips(tick.pips);
                        positionItem.setSide(tick.side);
                        positionItem.setPl(tick.pl);
                        //System.out.println("Position Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    PositionItem positionItem = new PositionItem(security,account,tick.exposure,tick.price,tick.pips,tick.side,tick.pl,null);
                    MainActivity.positionItemList.add(positionItem);
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
            if (ArthikaHFT.ORDERTYPE_INFLUX.equals(tick.status)) {
                continue;
            }
            if (ArthikaHFT.ORDERTYPE_PENDING.equals(tick.status)) {
                synchronized(MainActivity.pendingOrderList) {
                    boolean found = false;
                    for (OrderItem order : MainActivity.pendingOrderList) {
                        if (orderid.equals(order.getOrderid())) {
                            order.setFixid(tick.fixid);
                            order.setSecurity(tick.security);
                            order.setTinterface(tick.tinterface);
                            order.setQuantity(tick.quantity);
                            order.setSide(tick.side);
                            order.setLimitprice(tick.limitprice);
                            order.setPips(tick.pips);
                            order.setCommission(tick.commission);
                            order.setCommcurrency(tick.commcurrency);
                            order.setFinishedprice(tick.finishedprice);
                            order.setFinishedquantity(tick.finishedquantity);
                            order.setStatus(tick.status);
                            order.setType(tick.type);
                            order.setReason(tick.reason);
                            System.out.println("Pending Order Modified");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        OrderItem order = new OrderItem(tick.orderid, tick.fixid, tick.tinterface, tick.security, tick.quantity, tick.limitprice, tick.pips, tick.side, tick.type, tick.finishedprice, tick.finishedquantity, tick.commission, tick.commcurrency, tick.status, tick.reason);
                        MainActivity.pendingOrderList.add(0,order);
                        System.out.println("Pending Order Added");
                        if (MainActivity.pendingOrderList.size() > MainActivity.MAX_PENDING_ORDERS) {
                            MainActivity.pendingOrderList.remove(MainActivity.pendingOrderList.size()-1);
                        }
                    }
                    MainActivity.pendingOrderChanged = true;
                }
            }
            else{
                synchronized(MainActivity.pendingOrderList) {
                    OrderItem pendingOrder = null;
                    for (OrderItem order : MainActivity.pendingOrderList){
                        if (orderid.equals(order.getOrderid())) {
                            pendingOrder = order;
                            break;
                        }
                    }
                    if (pendingOrder!=null){
                        MainActivity.pendingOrderList.remove(pendingOrder);
                        MainActivity.pendingOrderChanged = true;
                        System.out.println("Pending Order Deleted");
                    }
                }
                boolean found = false;
                synchronized(MainActivity.closedOrderList) {
                    for (OrderItem order : MainActivity.closedOrderList){
                        if (orderid.equals(order.getOrderid())) {
                            order.setFixid(tick.fixid);
                            order.setSecurity(tick.security);
                            order.setTinterface(tick.tinterface);
                            order.setQuantity(tick.quantity);
                            order.setSide(tick.side);
                            order.setLimitprice(tick.limitprice);
                            order.setPips(tick.pips);
                            order.setCommission(tick.commission);
                            order.setCommcurrency(tick.commcurrency);
                            order.setFinishedprice(tick.finishedprice);
                            order.setFinishedquantity(tick.finishedquantity);
                            order.setStatus(tick.status);
                            order.setType(tick.type);
                            order.setReason(tick.reason);
                            System.out.println("Closed Order Modified");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        OrderItem order = new OrderItem(tick.orderid, tick.fixid, tick.tinterface, tick.security, tick.quantity, tick.limitprice, tick.pips, tick.side, tick.type, tick.finishedprice, tick.finishedquantity, tick.commission, tick.commcurrency, tick.status, tick.reason);
                        MainActivity.closedOrderList.add(0,order);
                        System.out.println("Closed Order Added");
                        if (MainActivity.closedOrderList.size() > MainActivity.MAX_CLOSED_ORDERS) {
                            MainActivity.closedOrderList.remove(MainActivity.closedOrderList.size()-1);
                        }
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
