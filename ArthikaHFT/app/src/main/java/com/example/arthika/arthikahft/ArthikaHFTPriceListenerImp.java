package com.example.arthika.arthikahft;

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
        for (ArthikaHFT.priceTick tick : priceTickList){
            //System.out.println("Security: " + tick.security + " Price: " + tick.price + " Side: " + tick.side + " Liquidity: " + tick.liquidity);
            for (int i=0; i<MainActivity.secs.size(); i++){
                if (tick.security.equals(MainActivity.secs.get(i))){
                    if (tick.side.equals("ask")){
                        MainActivity.prices[(i+1)*MainActivity.PRICE_COLUMNS+1]=String.format("%." + tick.pips + "f", tick.price);
                        if (tick.security.equals(PricePop.securitySelected)){
                            PricePop.asklist.add(tick.price);
                            PricePop.intervallist.add(MainActivity.updateTime);
                        }
                    }
                    else{
                        MainActivity.prices[(i+1)*MainActivity.PRICE_COLUMNS+2]=String.format("%." + tick.pips + "f", tick.price);
                        if (tick.security.equals(PricePop.securitySelected)) {
                            PricePop.bidlist.add(tick.price);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void assetPositionEvent(List<ArthikaHFT.assetPositionTick> assetPositionTickList) {
        for (ArthikaHFT.assetPositionTick tick : assetPositionTickList){
            System.out.println("Asset: " + tick.asset + " Account: " + tick.account + " Equity: " + tick.equity + " Exposure: " + tick.exposure);
            String asset = tick.asset;
            boolean found = false;
            for (int i=0; i<MainActivity.assetArray.size(); i=i+MainActivity.ASSET_COLUMNS) {
                if (asset.equals(MainActivity.assetArray.get(i))) {
                    MainActivity.assetArray.set(i + 1, String.valueOf(tick.exposure));
                    System.out.println("Asset Modified");
                    found = true;
                    break;
                }
            }
            if (!found){
                MainActivity.assetArray.add(asset);
                MainActivity.assetArray.add(String.valueOf(tick.exposure));
                System.out.println("Asset Added");
            }
        }
    }

    @Override
    public void securityPositionEvent(List<ArthikaHFT.securityPositionTick> securityPositionTickList) {
        for (ArthikaHFT.securityPositionTick tick : securityPositionTickList){
            System.out.println("Security: " + tick.security + " Account: " + tick.account + " Equity: " + tick.equity + " Exposure: " + tick.exposure + " Price: " + tick.price + " Pips: " + tick.pips);
            String security = tick.security;
            boolean found = false;
            for (int i=0; i<MainActivity.positionArray.size(); i=i+MainActivity.POSITION_COLUMNS) {
                if (security.equals(MainActivity.positionArray.get(i))) {
                    MainActivity.positionArray.set(i + 1, String.valueOf(tick.exposure));
                    MainActivity.positionArray.set(i + 2, tick.side);
                    MainActivity.positionArray.set(i + 3, String.format("%." + tick.pips + "f", tick.price));
                    System.out.println("Position Modified");
                    found = true;
                    break;
                }
            }
            if (!found){
                MainActivity.positionArray.add(security);
                MainActivity.positionArray.add(String.valueOf(tick.exposure));
                MainActivity.positionArray.add(tick.side);
                MainActivity.positionArray.add(String.format("%." + tick.pips + "f", tick.price));
                System.out.println("Position Added");
            }
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
            System.out.println("TempId: " + tick.tempid + " OrderId: " + tick.orderid + " Security: " + tick.security + " Account: " + tick.account + " Quantity: " + tick.quantity + " Type: " + tick.type + " Side: " + tick.side + " Status: " + tick.status);
            String orderid = tick.orderid;
            if ("in flux".equals(tick.status)) {
                continue;
            }
            if ("pending".equals(tick.status)) {
                boolean found = false;
                for (int i = 0; i < MainActivity.pendingOrderArray.size(); i = i+MainActivity.PENDINGORDER_COLUMNS) {
                    if (orderid.equals(MainActivity.pendingOrderArray.get(i))) {
                        MainActivity.pendingOrderArray.set(i + 1, tick.security);
                        MainActivity.pendingOrderArray.set(i + 2, String.valueOf(tick.quantity));
                        MainActivity.pendingOrderArray.set(i + 3, tick.side);
                        MainActivity.pendingOrderArray.set(i + 4, String.format("%." + tick.pips + "f", tick.priceatstart));
                        MainActivity.pendingOrderArray.set(i + 5, tick.status);
                        System.out.println("Pending Order Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MainActivity.pendingOrderArray.add(0, orderid);
                    MainActivity.pendingOrderArray.add(1, tick.security);
                    MainActivity.pendingOrderArray.add(2, String.valueOf(tick.quantity));
                    MainActivity.pendingOrderArray.add(3, tick.side);
                    MainActivity.pendingOrderArray.add(4, String.format("%." + tick.pips + "f", tick.priceatstart));
                    MainActivity.pendingOrderArray.add(5, tick.status);
                    System.out.println("Pending Order Added");
                }
            }
            else{
                for (int i = 0; i < MainActivity.pendingOrderArray.size(); i = i+MainActivity.PENDINGORDER_COLUMNS) {
                    if (orderid.equals(MainActivity.pendingOrderArray.get(i))) {
                        for (int j = 0; j < MainActivity.PENDINGORDER_COLUMNS; j++) {
                            MainActivity.pendingOrderArray.remove(i);
                        }
                        System.out.println("Pending Order Deleted");
                    }
                }
                boolean found = false;
                for (int i = 0; i < MainActivity.closedOrderArray.size(); i = i+MainActivity.CLOSEDORDER_COLUMNS) {
                    if (orderid.equals(MainActivity.closedOrderArray.get(i))) {
                        MainActivity.closedOrderArray.set(i + 1, tick.security);
                        MainActivity.closedOrderArray.set(i + 2, String.valueOf(tick.quantity));
                        MainActivity.closedOrderArray.set(i + 3, tick.side);
                        MainActivity.closedOrderArray.set(i + 4, String.format("%." + tick.pips + "f", tick.priceatstart));
                        MainActivity.closedOrderArray.set(i + 5, tick.status);
                        System.out.println("Order Modified");
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    MainActivity.closedOrderArray.add(0, orderid);
                    MainActivity.closedOrderArray.add(1, tick.security);
                    MainActivity.closedOrderArray.add(2, String.valueOf(tick.quantity));
                    MainActivity.closedOrderArray.add(3, tick.side);
                    MainActivity.closedOrderArray.add(4, String.format("%." + tick.pips + "f", tick.priceatstart));
                    MainActivity.closedOrderArray.add(5, tick.status);
                    System.out.println("Order Added");
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
