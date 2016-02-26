package com.example.arthika.arthikahft;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    public static Context context;

    public static boolean ssl;
    public static ArthikaHFT wrapper;
    public static String[] domainList;
    public static String domain;
    public static String url_stream;
    public static String url_polling;
    public static String url_challenge;
    public static String url_token;
    public static String user;
    public static String password;
    public static String authentication_port;
    public static String request_port;
    public static int interval;

    public static String ssl_authentication_port;
    public static String ssl_request_port;
    public static String ssl_cert;

    static int width;
    static boolean started;
    static long priceStreamingId;
    static long orderStreamingId;
    static long positionStreamingId;
    static String[] prices;
    static List<String> secs;
    static String[] secsAll;
    static List<Boolean> secsSelected;
    static final List<String> tiList = new ArrayList<>();
    static final List<String> tiAllList = new ArrayList<>();
    static final List<String> accountList = new ArrayList<>();
    static final Map<String,String> accountTIMap = new HashMap<>();
    static String[] amountList;
    static String[] typeList;
    static String[] validityList;
    static Integer[] intervalList;
    static String updateTime;
    static AlertDialog alertCancelOrder;
    static AlertDialog alertCancelAllOrders;
    static AlertDialog alertClosePosition;
    static AlertDialog connectionAlert;
    static Timer timer;
    static MyTimerTask myTimerTask;
    static final List<OrderItem> pendingOrderList = new ArrayList<>();
    static final List<OrderItem> closedOrderList = new ArrayList<>();
    static final List<String> pendingOrderShowArray = new ArrayList<>();
    static final List<String> closedOrderShowArray = new ArrayList<>();
    static final String[] accountingArray =new String[4];
    static final List<PositionItem> positionItemList = new ArrayList<>();
    static final List<AssetItem> assetItemList = new ArrayList<>();
    static final List<String> positionShowArray = new ArrayList<>();
    static final List<String> assetShowArray = new ArrayList<>();
    static List<ClosePositionItem> closePositionItemList = new ArrayList<>();
    static boolean pendingOrderChanged;
    static boolean closedOrderChanged;
    static boolean accountingChanged;
    static boolean positionChanged;
    static boolean assetChanged;
    static boolean tiListChanged;
    static boolean accountListChanged;
    static String accountPositionSelected;
    static String accountAssetSelected;
    static String tiPendingOrderSelected;
    static String tiClosedOrderSelected;
    static String m2mcurrency;

    public static final String AGREGATED = "<AGGREGATED>";
    public static final String ALL = "ALL";
    public static final int DEFAULT_PAD = 16;
    public static final int PRICE_COLUMNS = 3;
    public static final int PENDINGORDER_COLUMNS = 9;
    public static final int CLOSEDORDER_COLUMNS = 7;
    public static final int ACCOUNTING_COLUMNS = 4;
    public static final int POSITION_COLUMNS = 6;
    public static final int ASSET_COLUMNS = 4;
    public static final int MAX_PENDING_ORDERS = 50;
    public static final int MAX_CLOSED_ORDERS = 50;
    public static final int MAX_MESSAGE_ORDERS = 8;
    private static final int REFRESH_TIME = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // get properties from file
        getProperties();

        domainList = new String[]{"https://demo.arthikatrading.com", "http://demo.arthikatrading.com", "http://production.koiosinvestments.com"};
        domain = domainList[0];
        started = false;
        ssl=true;
        updateTime = "";

        secsAll = new String[]{"EUR/USD", "EUR/GBP", "GBP/USD", "USD/JPY", "EUR/JPY", "GBP/JPY", "AUD/USD", "USD/CAD"};
        secs = new ArrayList<>();
        secsSelected = new ArrayList<>();
        for (String ignored : secsAll) {
            secsSelected.add(true);
        }
        amountList = new String[]{"100K", "200K", "500K", "1M", "2M", "5M", "10M"};
        typeList = new String[]{ArthikaHFT.TYPE_MARKET, ArthikaHFT.TYPE_LIMIT};
        validityList = new String[]{ArthikaHFT.VALIDITY_FILLORKILL, ArthikaHFT.VALIDITY_DAY, ArthikaHFT.VALIDITY_GOODTILLCANCEL, ArthikaHFT.VALIDITY_INMEDIATEORCANCEL};
        intervalList = new Integer[]{0, 100, 200, 500, 1000, 2000, 5000, 10000};

        refreshSettings();

        AlertDialog.Builder alertCancelOrderBuilder = new AlertDialog.Builder(this);
        alertCancelOrderBuilder.setCancelable(true);
        alertCancelOrderBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        cancelOrder();
                    }
                });
        alertCancelOrderBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertCancelOrder = alertCancelOrderBuilder.create();

        AlertDialog.Builder alertCancelAllOrdersBuilder = new AlertDialog.Builder(this);
        alertCancelAllOrdersBuilder.setCancelable(true);
        alertCancelAllOrdersBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        cancelAllOrders();
                    }
                });
        alertCancelAllOrdersBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertCancelAllOrders = alertCancelAllOrdersBuilder.create();



        AlertDialog.Builder alertClosePositionBuilder = new AlertDialog.Builder(this);
        alertClosePositionBuilder.setCancelable(true);
        alertClosePositionBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        closePositions();
                    }
                });
        alertClosePositionBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertClosePosition = alertClosePositionBuilder.create();

        AlertDialog.Builder connectionAlertBuilder = new AlertDialog.Builder(this);
        connectionAlertBuilder.setCancelable(true);
        connectionAlertBuilder.setNegativeButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        refreshSettings();
                        dialog.cancel();
                    }
                });
        connectionAlert = connectionAlertBuilder.create();

        myTimerTask = new MyTimerTask();

        //getOrder();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsPop.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (started) {
            PricesFragment.startButton.setEnabled(true);
            PricesFragment.stopButton.setEnabled(false);
            started = false;
            closeStreaming();
        }
        super.onStop();
    }

    /*
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    */

    public static void getProperties(){
        /*
        Properties prop = new Properties();
        InputStream input = null;
        try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			url_stream = prop.getProperty("url-stream");
			url_polling = prop.getProperty("url-polling");
			url_challenge = prop.getProperty("url-challenge");
			url_token = prop.getProperty("url-token");
			user = prop.getProperty("user");
			password = prop.getProperty("password");
			interval = Integer.parseInt(prop.getProperty("interval"));
			if (ssl){
				domain = prop.getProperty("ssl-domain");
				authentication_port = prop.getProperty("ssl-authentication-port");
				request_port = prop.getProperty("ssl-request-port");
				ssl_cert = prop.getProperty("ssl-cert");
			}
			else{
				domain = prop.getProperty("domain");
				authentication_port = prop.getProperty("authentication-port");
				request_port = prop.getProperty("request-port");
			}
		}
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        */
        authentication_port="81";
        request_port="81";
        url_stream="/cgi-bin/IHFTRestStreamer";
        url_polling="/fcgi-bin/IHFTRestAPI";
        url_challenge="/fcgi-bin/IHFTRestAuth/getAuthorizationChallenge";
        url_token="/fcgi-bin/IHFTRestAuth/getAuthorizationToken";
        interval=0;
        user="demo";
        password="demo";

        ssl_authentication_port="8081";
        ssl_request_port="8081";
        ssl_cert="http://secure2.alphassl.com/cacert/gsalphasha2g2r1.crt";
    }

    public static void refreshSettings(){
        if (started) {
            started = false;
            synchronized(ArthikaHFT.wrapperLock) {
                try {
                    closeStreaming();
                    ArthikaHFT.wrapperLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (ssl) {
            wrapper = new ArthikaHFT(domain, url_stream, url_polling, url_challenge, url_token, user, password, ssl_authentication_port, ssl_request_port, true, ssl_cert);
        }
        else{
            wrapper = new ArthikaHFT(domain, url_stream, url_polling, url_challenge, url_token, user, password, authentication_port, request_port, false, ssl_cert);
        }
        clearData();
    }

    private static void clearData(){
        secs.clear();
        for (int i=0; i<secsAll.length; i++){
            if (secsSelected.get(i)){
                secs.add(secsAll[i]);
            }
        }
        prices = new String[PRICE_COLUMNS * (secs.size() + 1)];
        prices[0] = context.getString(R.string.security).toUpperCase();
        prices[1] = ArthikaHFT.SIDE_ASK.toUpperCase();
        prices[2] = ArthikaHFT.SIDE_BID.toUpperCase();
        PricesFragment.refreshSettingsText();
        for (int i = 0; i < secs.size(); i++) {
            prices[(i + 1) * PRICE_COLUMNS] = secs.get(i);
            prices[(i + 1) * PRICE_COLUMNS + 1] = Utils.doubleToString(0);
            prices[(i + 1) * PRICE_COLUMNS + 2] = Utils.doubleToString(0);
        }
        accountList.clear();
        accountList.add(ALL);
        tiList.clear();
        tiAllList.clear();
        tiAllList.add(ALL);
        accountTIMap.clear();
        tiPendingOrderSelected = ALL;
        tiClosedOrderSelected = ALL;
        accountPositionSelected = ALL;
        accountAssetSelected = ALL;
        pendingOrderList.clear();
        closedOrderList.clear();
        pendingOrderShowArray.clear();
        closedOrderShowArray.clear();
        EquityPop.equityStrategyList.clear();
        EquityPop.equityPoolList.clear();
        EquityPop.intervalList.clear();
        accountingArray[0]=Utils.doubleToString(0);
        accountingArray[1]=Utils.doubleToString(0);
        accountingArray[2]=Utils.doubleToString(0);
        accountingArray[3]=Utils.doubleToString(0);
        positionItemList.clear();
        assetItemList.clear();
        positionShowArray.clear();
        assetShowArray.clear();
        pendingOrderChanged = true;
        closedOrderChanged = true;
        accountingChanged = true;
        positionChanged = true;
        assetChanged = true;
        m2mcurrency = null;
    }

    private static void doAuthentication() {
        new doAuthenticationConnection().execute();
    }

    private static class doAuthenticationConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            doAuthenticationConnect();
            return null;
        }

    }

    private static void doAuthenticationConnect(){
        try {
            wrapper.doAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getAccount() {
        new getAccountConnection().execute();
    }

    private static class getAccountConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            getAccountConnect();
            return null;
        }

    }

    private static void getAccountConnect() {
        try {
            final List<ArthikaHFT.accountTick> accountTickList = wrapper.getAccount();
            accountList.clear();
            accountList.add(ALL);
            for (ArthikaHFT.accountTick tick : accountTickList){
                accountList.add(tick.name);
            }
            accountListChanged=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getInterface() {
        new getInterfaceConnection().execute();
    }

    private static class getInterfaceConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            getInterfaceConnect();
            return null;
        }

    }

    private static void getInterfaceConnect() {
        try {
            final List<ArthikaHFT.tinterfaceTick> tinterfaceTickList = wrapper.getInterface();
            tiList.clear();
            tiAllList.clear();
            tiAllList.add(ALL);
            for (ArthikaHFT.tinterfaceTick tick : tinterfaceTickList){
                tiList.add(tick.name);
                tiAllList.add(tick.name);
                if (accountTIMap.get(tick.account)==null){
                    accountTIMap.put(tick.account, tick.name);
                }
                tiListChanged=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelOrder() {
        new cancelOrderConnection().execute();
    }

    private class cancelOrderConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            cancelOrderConnect();
            return null;
        }

    }

    private void cancelOrderConnect() {
        String fixid = pendingOrderShowArray.get(OrderFragment.cellOrderSelected - 6);
        try {
            wrapper.cancelOrder(Collections.singletonList(fixid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelAllOrders() {
        new cancelAllOrdersConnection().execute();
    }

    private class cancelAllOrdersConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            cancelAllOrdersConnect();
            return null;
        }

    }

    private void cancelAllOrdersConnect() {
        List<String> fixidList = new ArrayList<>();
        synchronized (pendingOrderList){
            for (OrderItem order : pendingOrderList){
                fixidList.add(order.getFixid());
            }
        }
        try {
            wrapper.cancelOrder(fixidList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePositions() {
        new closePositionsConnection().execute();
    }

    private class closePositionsConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            closePositionsConnect();
            return null;
        }

    }

    private void closePositionsConnect() {
        List<ArthikaHFT.orderRequest> orderlist = new ArrayList<>();
        for (ClosePositionItem closePositionItem : closePositionItemList){
            String ti = accountTIMap.get(closePositionItem.getAccount());
            if (ti==null){
                continue;
            }
            ArthikaHFT.orderRequest order = new ArthikaHFT.orderRequest();
            order.security = closePositionItem.getSecurity();
            order.tinterface = ti;
            order.quantity = (int) closePositionItem.getQuantity();
            order.side = closePositionItem.getSide();
            order.type = ArthikaHFT.TYPE_MARKET;
            order.timeinforce = ArthikaHFT.VALIDITY_FILLORKILL;
            orderlist.add(order);
        }
        try {
            MainActivity.wrapper.setOrder(orderlist);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void closeStreaming() {
        new closeStreamingConnection().execute();
    }

    private static class closeStreamingConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            closeStreamingConnect();
            return null;
        }

    }

    private static void closeStreamingConnect() {
        try {
            synchronized(ArthikaHFT.wrapperLock) {
                System.out.println("Finishing :" + priceStreamingId);
                wrapper.getPriceEnd(priceStreamingId);
                System.out.println("Finishing :" + orderStreamingId);
                wrapper.getOrderEnd(orderStreamingId);
                System.out.println("Finishing :" + positionStreamingId);
                wrapper.getPositionEnd(positionStreamingId);
                ArthikaHFT.wrapperLock.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (PricesFragment.pricesGridView!=null) {
                        if (started) {
                            // update Time
                            if (updateTime != null && !updateTime.equals("")) {
                                long timelong = Double.valueOf(Double.valueOf(updateTime) * 1000).longValue();
                                PricesFragment.updateTimeTextView.setText(Utils.dateToString(timelong));
                            }
                        }
                        // update prices table
                        AdapterPrices priceAdapter = (AdapterPrices) PricesFragment.pricesGridView.getAdapter();
                        priceAdapter.notifyDataSetChanged();
                        PricesFragment.pricesGridView.setAdapter(priceAdapter);
                        PricePop.refresh();
                    }

                    // update orders
                    if (OrderFragment.pendingOrderGridView!=null) {
                        // update tinterface list
                        if (tiListChanged){
                            tiListChanged = false;
                            if (OrderFragment.tiPendingOrderSpinner!=null) {
                                ArrayAdapter tiPendingOrderAdapter = (ArrayAdapter) OrderFragment.tiPendingOrderSpinner.getAdapter();
                                tiPendingOrderAdapter.notifyDataSetChanged();
                                OrderFragment.tiPendingOrderSpinner.setAdapter(tiPendingOrderAdapter);
                            }
                            if (OrderFragment.tiClosedOrderSpinner!=null) {
                                ArrayAdapter tiClosedOrderAdapter = (ArrayAdapter) OrderFragment.tiClosedOrderSpinner.getAdapter();
                                tiClosedOrderAdapter.notifyDataSetChanged();
                                OrderFragment.tiClosedOrderSpinner.setAdapter(tiClosedOrderAdapter);
                            }
                        }
                        //update pending orders
                        if (pendingOrderChanged) {
                            pendingOrderChanged = false;
                            synchronized(pendingOrderList){
                                pendingOrderShowArray.clear();
                                for (OrderItem pendingOrder : pendingOrderList) {
                                    if (tiPendingOrderSelected.equals(pendingOrder.getTinterface()) || tiPendingOrderSelected.equals(ALL)) {
                                        pendingOrderShowArray.add(pendingOrder.getOrderid());
                                        pendingOrderShowArray.add(pendingOrder.getFixid());
                                        pendingOrderShowArray.add(pendingOrder.getSecurity());
                                        pendingOrderShowArray.add(Utils.doubleToString(pendingOrder.getQuantity()));
                                        pendingOrderShowArray.add(pendingOrder.getSide());
                                        pendingOrderShowArray.add(Utils.doubleToString(pendingOrder.getLimitprice(), pendingOrder.getPips()));
                                        pendingOrderShowArray.add(" " + context.getString(R.string.modify) + " ");
                                        pendingOrderShowArray.add(" " + context.getString(R.string.cancel) + " ");
                                        pendingOrderShowArray.add(" " + context.getString(R.string.info) + " ");
                                    }
                                }
                            }
                            try {
                                AdapterPendingOrders pendingOrderAdapter = (AdapterPendingOrders) OrderFragment.pendingOrderGridView.getAdapter();
                                pendingOrderAdapter.notifyDataSetChanged();
                                OrderFragment.pendingOrderGridView.setAdapter(pendingOrderAdapter);
                            }
                            catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                        // update closed orders
                        if (closedOrderChanged) {
                            closedOrderChanged = false;
                            synchronized (closedOrderList) {
                                closedOrderShowArray.clear();
                                for (OrderItem closedOrder : closedOrderList){
                                    if (tiClosedOrderSelected.equals(closedOrder.getTinterface()) || tiClosedOrderSelected.equals(ALL)) {
                                        closedOrderShowArray.add(closedOrder.getOrderid());
                                        //closedOrderShowArray.add(closedOrder.getFixid());
                                        closedOrderShowArray.add(closedOrder.getSecurity());
                                        closedOrderShowArray.add(Utils.doubleToString(closedOrder.getFinishedquantity()));
                                        closedOrderShowArray.add(closedOrder.getSide());
                                        closedOrderShowArray.add(Utils.doubleToString(closedOrder.getFinishedprice(), closedOrder.getPips()));
                                        String status = closedOrder.getStatus();
                                        if (status.contains(" ")){
                                            status = status.substring(0,status.indexOf(" "));
                                        }
                                        closedOrderShowArray.add(status);
                                        closedOrderShowArray.add(" " + context.getString(R.string.info) + " ");
                                    }
                                }
                                try{
                                    AdapterClosedOrders closedOrderAdapter = (AdapterClosedOrders) OrderFragment.closedOrderGridView.getAdapter();
                                    closedOrderAdapter.notifyDataSetChanged();
                                    OrderFragment.closedOrderGridView.setAdapter(closedOrderAdapter);
                                }
                                catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }

                    // updates position & cash
                    if (PositionFragment.positionGridView!=null) {
                        // update account list
                        if (accountListChanged){
                            accountListChanged = false;
                            if (PositionFragment.accountPositionSpinner!=null) {
                                ArrayAdapter accountPositionAdapter = (ArrayAdapter) PositionFragment.accountPositionSpinner.getAdapter();
                                accountPositionAdapter.notifyDataSetChanged();
                                PositionFragment.accountPositionSpinner.setAdapter(accountPositionAdapter);
                            }
                            if (PositionFragment.accountAssetSpinner!=null) {
                                ArrayAdapter accountAssetAdapter = (ArrayAdapter) PositionFragment.accountAssetSpinner.getAdapter();
                                accountAssetAdapter.notifyDataSetChanged();
                                PositionFragment.accountAssetSpinner.setAdapter(accountAssetAdapter);
                            }
                        }
                        // update accounting data
                        if (accountingChanged) {
                            accountingChanged = false;
                            if (m2mcurrency!=null){
                                PositionFragment.accountingCurrencyTextView.setText("(" + m2mcurrency + ")");
                            }
                            synchronized (accountingArray) {
                                ArrayAdapter accountingAdapter = (ArrayAdapter) PositionFragment.accountingGridView.getAdapter();
                                accountingAdapter.notifyDataSetChanged();
                                PositionFragment.accountingGridView.setAdapter(accountingAdapter);
                            }
                        }
                        EquityPop.refresh();
                        // update position
                        if (positionChanged) {
                            positionChanged = false;
                            synchronized (positionItemList) {
                                positionShowArray.clear();
                                for (PositionItem positionItem : MainActivity.positionItemList) {
                                    if (accountPositionSelected.equals(positionItem.getAccount())) {
                                        positionShowArray.add(positionItem.getSecurity());
                                        positionShowArray.add(Utils.doubleToString(positionItem.getExposure()));
                                        positionShowArray.add("@" + Utils.doubleToString(positionItem.getPrice(), positionItem.getPips()));
                                        positionShowArray.add(positionItem.getSide());
                                        positionShowArray.add(Utils.doubleToString(positionItem.getPl()));
                                        positionShowArray.add(" " + context.getString(R.string.close) + " ");
                                    }
                                }
                            }
                            AdapterPosition positionAdapter = (AdapterPosition) PositionFragment.positionGridView.getAdapter();
                            positionAdapter.notifyDataSetChanged();
                            PositionFragment.positionGridView.setAdapter(positionAdapter);
                        }
                        // update asset
                        if (assetChanged) {
                            assetChanged = false;
                            synchronized (assetItemList) {
                                assetShowArray.clear();
                                for (AssetItem assetItem : MainActivity.assetItemList) {
                                    if (accountAssetSelected.equals(assetItem.getAccount())) {
                                        assetShowArray.add(assetItem.getAsset());
                                        assetShowArray.add(Utils.doubleToString(assetItem.getExposure()));
                                        assetShowArray.add(Utils.doubleToString(assetItem.getTotalrisk()));
                                        assetShowArray.add(Utils.doubleToString(assetItem.getPl()));
                                    }
                                }
                            }
                            ArrayAdapter assetAdapter = (ArrayAdapter) PositionFragment.assetGridView.getAdapter();
                            assetAdapter.notifyDataSetChanged();
                            PositionFragment.assetGridView.setAdapter(assetAdapter);
                        }
                    }

                }
            });
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position==0) {
                return PricesFragment.newInstance();
            }
            if (position==1) {
                return OrderFragment.newInstance();
            }
            if (position==2) {
                return PositionFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.prices);
                case 1:
                    return context.getString(R.string.orders);
                case 2:
                    return context.getString(R.string.positions);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PricesFragment extends Fragment {

        static GridView pricesGridView;
        static TextView domainTextView;
        static TextView userTextView;
        static TextView updateTimeTextView;
        static Button startButton;
        static Button stopButton;
        static int cellSelected;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PricesFragment newInstance() {
            return new PricesFragment();
        }

        public PricesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_prices, container, false);
            System.out.println("CREATING PricesFragment");

            pricesGridView = (GridView) view.findViewById(R.id.pricesGridView);

            domainTextView = (TextView) view.findViewById(R.id.domainTextView);
            userTextView = (TextView) view.findViewById(R.id.userTextView);
            startButton = (Button) view.findViewById(R.id.startButton);
            stopButton = (Button) view.findViewById(R.id.stopButton);
            updateTimeTextView = (TextView) view.findViewById(R.id.updateTimeTextView);

            refreshSettingsText();

            AdapterPrices priceAdapter = new AdapterPrices(this.getContext(), R.layout.my_pricesgridview_format, prices);
            priceAdapter.notifyDataSetChanged();
            pricesGridView.setAdapter(priceAdapter);

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    started = true;
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    updateTime = "";
                    updateTimeTextView.setText(R.string.getting_prices);
                    clearData();
                    try {
                        synchronized (ArthikaHFT.wrapperLock) {
                            doAuthentication();
                            ArthikaHFT.wrapperLock.wait();
                        }
                        getAccount();
                        getInterface();

                        priceStreamingId = wrapper.getPriceBegin(secs, null, ArthikaHFT.GRANULARITY_TOB, 1, interval, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + priceStreamingId);
                        orderStreamingId = wrapper.getOrderBegin(null, null, null, interval, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + orderStreamingId);
                        positionStreamingId = wrapper.getPositionBegin(null, null, null, interval, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + positionStreamingId);
                        if (priceStreamingId==-1 || orderStreamingId==-1 || positionStreamingId==-1){
                            connectionAlert.setMessage(context.getString(R.string.conection_message));
                            connectionAlert.show();
                            return;
                        }
                        if (timer == null) {
                            timer = new Timer();
                            timer.schedule(myTimerTask, 0, REFRESH_TIME);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    started = false;
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    synchronized(ArthikaHFT.wrapperLock) {
                        try {
                            closeStreaming();
                            ArthikaHFT.wrapperLock.wait();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    updateTimeTextView.setText(R.string.streaming_stopped);
                }
            });

            pricesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    if (!started){
                        return;
                    }
                    if (position > (PRICE_COLUMNS - 1)) {
                        cellSelected = position;
                        if ((cellSelected % PRICE_COLUMNS) == 0) {
                            startActivity(new Intent(v.getContext(), PricePop.class));
                            PricePop.securitySelected = prices[cellSelected];
                        }
                        if ((cellSelected % PRICE_COLUMNS) == 1) {
                            startActivity(new Intent(v.getContext(), TradePop.class));
                            TradePop.securitySelected = prices[cellSelected-1];
                            TradePop.side = ArthikaHFT.SIDE_BUY;
                            TradePop.price = prices[cellSelected];
                        }
                        if ((cellSelected % PRICE_COLUMNS) == 2) {
                            startActivity(new Intent(v.getContext(), TradePop.class));
                            TradePop.securitySelected = prices[cellSelected-2];
                            TradePop.side = ArthikaHFT.SIDE_SELL;
                            TradePop.price = prices[cellSelected];
                        }
                    }
                }
            });

            return view;
        }

        public static void refreshSettingsText(){
            if (domainTextView!=null) {
                domainTextView.setText(context.getString(R.string.domain) + " " + domain);
                userTextView.setText(context.getString(R.string.strategy) + " " + user);
                if (started) {
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } else {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    updateTimeTextView.setText(R.string.click_start);
                }
            }
            if (pricesGridView!=null) {
                AdapterPrices priceAdapter = (AdapterPrices) pricesGridView.getAdapter();
                if (priceAdapter!=null) {
                    Context context = priceAdapter.getContext();
                    priceAdapter = new AdapterPrices(context, R.layout.my_pricesgridview_format, prices);
                    priceAdapter.notifyDataSetChanged();
                    pricesGridView.setAdapter(priceAdapter);
                }
            }
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OrderFragment extends Fragment {

        static GridView pendingOrderHeaderGridView;
        static GridView pendingOrderGridView;
        static GridView closedOrderHeaderGridView;
        static GridView closedOrderGridView;
        static Spinner tiPendingOrderSpinner;
        static Spinner tiClosedOrderSpinner;
        static TextView cancelAllPendingOrdersTextView;
        static TextView clearClosedOrdersTextView;
        static int cellOrderSelected;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static OrderFragment newInstance() {
            return new OrderFragment();
        }

        public OrderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_order, container, false);
            System.out.println("CREATING OrderFragment");

            pendingOrderHeaderGridView = (GridView) view.findViewById(R.id.pendingOrderHeaderGridView);
            pendingOrderHeaderGridView.setNumColumns(PENDINGORDER_COLUMNS);
            pendingOrderHeaderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) * 2, 0,  -((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) / 2, 0);
            //pendingOrderHeaderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) * 1, 0, 0, 0);
            List<String> pendingOrderHeaderArray = new ArrayList<> ();
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_orderid));
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_fixid));
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_security));
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_quantity));
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_side));
            pendingOrderHeaderArray.add(context.getString(R.string.order_info_price));
            pendingOrderHeaderArray.add(context.getString(R.string.modify));
            pendingOrderHeaderArray.add(context.getString(R.string.cancel));
            pendingOrderHeaderArray.add(context.getString(R.string.info));
            ArrayAdapter<String> pendingOrderHeaderAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridviewheader_format, pendingOrderHeaderArray);
            pendingOrderHeaderAdapter.notifyDataSetChanged();
            pendingOrderHeaderGridView.setAdapter(pendingOrderHeaderAdapter);

            pendingOrderGridView = (GridView) view.findViewById(R.id.pendingOrderGridView);
            pendingOrderGridView.setNumColumns(PENDINGORDER_COLUMNS);
            pendingOrderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) * 2, 0,  -((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) / 2, 0);
            //pendingOrderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2)) * 1, 0, 0, 0);
            AdapterPendingOrders pendingOrderAdapter = new AdapterPendingOrders(this.getContext(), R.layout.my_gridview_format, pendingOrderShowArray);
            pendingOrderAdapter.notifyDataSetChanged();
            pendingOrderGridView.setAdapter(pendingOrderAdapter);

            closedOrderHeaderGridView = (GridView) view.findViewById(R.id.closedOrderHeaderGridView);
            closedOrderHeaderGridView.setNumColumns(CLOSEDORDER_COLUMNS);
            closedOrderHeaderGridView.setPadding(-(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1), 0, -(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1) / 2, 0);
            List<String> closedOrderHeaderArray = new ArrayList<> ();
            closedOrderHeaderArray.add(context.getString(R.string.order_info_orderid));
            closedOrderHeaderArray.add(context.getString(R.string.order_info_security));
            closedOrderHeaderArray.add(context.getString(R.string.order_info_quantity));
            closedOrderHeaderArray.add(context.getString(R.string.order_info_side));
            closedOrderHeaderArray.add(context.getString(R.string.order_info_price));
            closedOrderHeaderArray.add(context.getString(R.string.order_info_status));
            closedOrderHeaderArray.add(context.getString(R.string.info));
            ArrayAdapter<String> closedOrderHeaderAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridviewheader_format, closedOrderHeaderArray);
            closedOrderHeaderAdapter.notifyDataSetChanged();
            closedOrderHeaderGridView.setAdapter(closedOrderHeaderAdapter);

            closedOrderGridView = (GridView) view.findViewById(R.id.closedOrderGridView);
            closedOrderGridView.setNumColumns(CLOSEDORDER_COLUMNS);
            closedOrderGridView.setPadding(-(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1), 0, -(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1) / 2, 0);
            AdapterClosedOrders closedOrderAdapter = new AdapterClosedOrders(this.getContext(), R.layout.my_gridview_format, closedOrderShowArray);
            closedOrderAdapter.notifyDataSetChanged();
            closedOrderGridView.setAdapter(closedOrderAdapter);

            tiPendingOrderSpinner = (Spinner) view.findViewById(R.id.tiPendingOrderSpinner);
            tiClosedOrderSpinner = (Spinner) view.findViewById(R.id.tiClosedOrderSpinner);
            //if (MainActivity.tilist!=null) {
            ArrayAdapter<String> tiPendingOrderAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, MainActivity.tiAllList);
            tiPendingOrderSpinner.setAdapter(tiPendingOrderAdapter);
            ArrayAdapter<String> tiClosedOrderAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, MainActivity.tiAllList);
            tiClosedOrderSpinner.setAdapter(tiClosedOrderAdapter);
            //}

            cancelAllPendingOrdersTextView = (TextView) view.findViewById(R.id.cancelAllPendingOrdersTextView);
            clearClosedOrdersTextView = (TextView) view.findViewById(R.id.clearClosedOrdersTextView);

            pendingOrderGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    synchronized (pendingOrderShowArray) {
                        try {
                            cellOrderSelected = position;
                            System.out.println("selected " + cellOrderSelected);
                            if ((cellOrderSelected % PENDINGORDER_COLUMNS) == (PENDINGORDER_COLUMNS - 3)) {
                                if (!started) {
                                    return;
                                }
                                System.out.println("modify " + pendingOrderShowArray.get(cellOrderSelected - 5));
                                startActivity(new Intent(v.getContext(), TradeModifyPop.class));
                                TradeModifyPop.fixidSelected = pendingOrderShowArray.get(cellOrderSelected - 5);
                                TradeModifyPop.securitySelected = pendingOrderShowArray.get(cellOrderSelected - 4);
                                TradeModifyPop.amountString = pendingOrderShowArray.get(cellOrderSelected - 3);
                                TradeModifyPop.side = pendingOrderShowArray.get(cellOrderSelected - 2);
                                TradeModifyPop.price = pendingOrderShowArray.get(cellOrderSelected - 1);
                            }
                            if ((cellOrderSelected % PENDINGORDER_COLUMNS) == (PENDINGORDER_COLUMNS - 2)) {
                                if (!started) {
                                    return;
                                }
                                System.out.println("cancel " + pendingOrderShowArray.get(cellOrderSelected - 6));
                                String alertMessage = context.getString(R.string.cancel_order_message1) + " " + pendingOrderShowArray.get(cellOrderSelected - 3) + " " + pendingOrderShowArray.get(cellOrderSelected - 4) + " " + pendingOrderShowArray.get(cellOrderSelected - 5) + " " + context.getString(R.string.at) + " " + pendingOrderShowArray.get(cellOrderSelected - 2) + "?";
                                alertCancelOrder.setMessage(alertMessage);
                                alertCancelOrder.show();
                            }
                            if ((cellOrderSelected % PENDINGORDER_COLUMNS) == (PENDINGORDER_COLUMNS - 1)) {
                                System.out.println("info " + pendingOrderShowArray.get(cellOrderSelected - 8));
                                startActivity(new Intent(v.getContext(), OrderInfoPop.class));
                                OrderInfoPop.orderInfoOrderIdSelected = pendingOrderShowArray.get(cellOrderSelected - 8);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            closedOrderGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    synchronized (closedOrderShowArray) {
                        try {
                            cellOrderSelected = position;
                            System.out.println("selected " + cellOrderSelected);
                            if ((cellOrderSelected % CLOSEDORDER_COLUMNS) == (CLOSEDORDER_COLUMNS - 1)) {
                                System.out.println("info " + closedOrderShowArray.get(cellOrderSelected - 6));
                                startActivity(new Intent(v.getContext(), OrderInfoPop.class));
                                OrderInfoPop.orderInfoOrderIdSelected = closedOrderShowArray.get(cellOrderSelected - 6);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            tiPendingOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    tiPendingOrderSelected = (String) tiPendingOrderSpinner.getSelectedItem();
                    pendingOrderChanged = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            tiClosedOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    tiClosedOrderSelected = (String) tiClosedOrderSpinner.getSelectedItem();
                    closedOrderChanged = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            cancelAllPendingOrdersTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!started) {
                        return;
                    }
                    synchronized (pendingOrderShowArray) {
                        alertCancelAllOrders.setMessage(context.getString(R.string.cancel_all_orders_message));
                        alertCancelAllOrders.show();
                    }
                }
            });

            clearClosedOrdersTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!started) {
                        return;
                    }
                    synchronized (closedOrderList) {
                        closedOrderList.clear();
                        closedOrderChanged = true;
                    }
                }
            });

            return view;
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PositionFragment extends Fragment {

        static Button equityButton;
        static TextView accountingCurrencyTextView;
        static GridView accountingGridView;
        static GridView accountingHeaderGridView;
        static GridView positionGridView;
        static GridView positionHeaderGridView;
        static GridView assetGridView;
        static GridView assetHeaderGridView;
        static Spinner accountPositionSpinner;
        static Spinner accountAssetSpinner;
        static TextView closeAllPositionsTextView;
        static int cellPositionSelected;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PositionFragment newInstance() {
            return new PositionFragment();
        }

        public PositionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_position, container, false);
            System.out.println("CREATING PositionFragment");


            accountingCurrencyTextView = (TextView) view.findViewById(R.id.accountingCurrencyTextView);
            equityButton = (Button) view.findViewById(R.id.equityButton);

            accountingHeaderGridView = (GridView) view.findViewById(R.id.accountingHeaderGridView);
            accountingHeaderGridView.setNumColumns(ACCOUNTING_COLUMNS);
            List<String> accountingHeaderArray = new ArrayList<> ();
            accountingHeaderArray.add(context.getString(R.string.position_info_strategypl));
            accountingHeaderArray.add(context.getString(R.string.position_info_totalequity));
            accountingHeaderArray.add(context.getString(R.string.position_info_usedmargin));
            accountingHeaderArray.add(context.getString(R.string.position_info_freemarginy));
            ArrayAdapter<String> accountingHeaderAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridviewheader_format, accountingHeaderArray);
            accountingHeaderAdapter.notifyDataSetChanged();
            accountingHeaderGridView.setAdapter(accountingHeaderAdapter);

            accountingGridView = (GridView) view.findViewById(R.id.accountingGridView);
            accountingGridView.setNumColumns(ACCOUNTING_COLUMNS);
            ArrayAdapter<String> accountingAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridview_format, accountingArray);
            accountingAdapter.notifyDataSetChanged();
            accountingGridView.setAdapter(accountingAdapter);

            positionHeaderGridView = (GridView) view.findViewById(R.id.positionHeaderGridView);
            positionHeaderGridView.setNumColumns(POSITION_COLUMNS);
            List<String> positionHeaderArray = new ArrayList<> ();
            positionHeaderArray.add(context.getString(R.string.position_info_security));
            positionHeaderArray.add(context.getString(R.string.position_info_position));
            positionHeaderArray.add("");
            positionHeaderArray.add(context.getString(R.string.position_info_side));
            positionHeaderArray.add(context.getString(R.string.position_info_pl));
            positionHeaderArray.add(context.getString(R.string.position_info_closepositions));
            ArrayAdapter<String> positionHeaderAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridviewheader_format, positionHeaderArray);
            positionHeaderAdapter.notifyDataSetChanged();
            positionHeaderGridView.setAdapter(positionHeaderAdapter);

            positionGridView = (GridView) view.findViewById(R.id.positionGridView);
            positionGridView.setNumColumns(POSITION_COLUMNS);
            AdapterPosition positionAdapter = new AdapterPosition(this.getContext(), R.layout.my_gridview_format, positionShowArray);
            positionAdapter.notifyDataSetChanged();
            positionGridView.setAdapter(positionAdapter);

            assetHeaderGridView = (GridView) view.findViewById(R.id.assetHeaderGridView);
            assetHeaderGridView.setNumColumns(ASSET_COLUMNS);
            List<String> assetHeaderArray = new ArrayList<> ();
            assetHeaderArray.add(context.getString(R.string.position_info_currency));
            assetHeaderArray.add(context.getString(R.string.position_info_exposure));
            assetHeaderArray.add(context.getString(R.string.position_info_totalrisk));
            assetHeaderArray.add(context.getString(R.string.position_info_pl));
            ArrayAdapter<String> assetHeaderAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridviewheader_format, assetHeaderArray);
            assetHeaderAdapter.notifyDataSetChanged();
            assetHeaderGridView.setAdapter(assetHeaderAdapter);

            assetGridView = (GridView) view.findViewById(R.id.assetGridView);
            assetGridView.setNumColumns(ASSET_COLUMNS);
            ArrayAdapter<String> assetAdapter = new ArrayAdapter<>(this.getContext(), R.layout.my_gridview_format, assetShowArray);
            assetAdapter.notifyDataSetChanged();
            assetGridView.setAdapter(assetAdapter);

            accountPositionSpinner = (Spinner) view.findViewById(R.id.accountPositionSpinner);
            accountAssetSpinner = (Spinner) view.findViewById(R.id.accountAssetSpinner);
            //if (MainActivity.accountlist!=null) {
            ArrayAdapter<String> accountPositionAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, MainActivity.accountList);
            accountPositionSpinner.setAdapter(accountPositionAdapter);
            ArrayAdapter<String> accountAssetAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, MainActivity.accountList);
            accountAssetSpinner.setAdapter(accountAssetAdapter);
            //}

            closeAllPositionsTextView = (TextView) view.findViewById(R.id.closeAllPositionsTextView);

            equityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!started) {
                        return;
                    }
                    startActivity(new Intent(v.getContext(), EquityPop.class));
                }
            });

            positionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    if (!started) {
                        return;
                    }
                    try {
                        cellPositionSelected = position;
                        if ((cellPositionSelected % POSITION_COLUMNS) == (POSITION_COLUMNS - 1)) {
                            String security = positionShowArray.get(cellPositionSelected - 5);
                            closePositionItemList.clear();
                            System.out.println("Close position " + security + accountPositionSelected);
                            synchronized (positionItemList) {
                                for (PositionItem positionItem : positionItemList) {
                                    if (positionItem.getExposure() <= 0) {
                                        continue;
                                    }
                                    if (security.equals(positionItem.getSecurity())) {
                                        if (!accountPositionSelected.equals(ALL) && positionItem.getAccount().equals(accountPositionSelected)) {
                                            if (positionItem.getSide().equals(ArthikaHFT.SIDE_BUY)) {
                                                ClosePositionItem closePositionItem = new ClosePositionItem(security, positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_SELL);
                                                closePositionItemList.add(closePositionItem);
                                            }
                                            if (positionItem.getSide().equals(ArthikaHFT.SIDE_SELL)) {
                                                ClosePositionItem closePositionItem = new ClosePositionItem(security, positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_BUY);
                                                closePositionItemList.add(closePositionItem);
                                            }
                                            break;
                                        }
                                        if (accountPositionSelected.equals(ALL) && !positionItem.getAccount().equals(ALL)) {
                                            if (positionItem.getSide().equals(ArthikaHFT.SIDE_BUY)) {
                                                ClosePositionItem closePositionItem = new ClosePositionItem(security, positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_SELL);
                                                closePositionItemList.add(closePositionItem);
                                            }
                                            if (positionItem.getSide().equals(ArthikaHFT.SIDE_SELL)) {
                                                ClosePositionItem closePositionItem = new ClosePositionItem(security, positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_BUY);
                                                closePositionItemList.add(closePositionItem);
                                            }
                                        }
                                    }
                                }
                            }
                            if (closePositionItemList.size() > 0) {
                                String alertMessage = context.getString(R.string.close_positions_message) + " " + security + "?:";
                                int n = 0;
                                for (ClosePositionItem closePositionItem : closePositionItemList) {
                                    alertMessage += "\n" + closePositionItem.getSide().toUpperCase() + " " + Utils.doubleToString(closePositionItem.getQuantity()) + " " + closePositionItem.getSecurity() + " " + context.getString(R.string.in) + " " + closePositionItem.getAccount();
                                    if (n++ > MAX_MESSAGE_ORDERS) {
                                        alertMessage += "\n...";
                                        break;
                                    }
                                }
                                alertClosePosition.setMessage(alertMessage);
                                alertClosePosition.show();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            closeAllPositionsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!started) {
                        return;
                    }
                    try {
                        closePositionItemList.clear();
                        System.out.println("Close All positions");
                        synchronized (positionItemList) {
                            for (PositionItem positionItem : positionItemList) {
                                if (positionItem.getExposure() <= 0) {
                                    continue;
                                }
                                if (!positionItem.getAccount().equals(ALL)) {
                                    if (positionItem.getSide().equals(ArthikaHFT.SIDE_BUY)) {
                                        ClosePositionItem closePositionItem = new ClosePositionItem(positionItem.getSecurity(), positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_SELL);
                                        closePositionItemList.add(closePositionItem);
                                    }
                                    if (positionItem.getSide().equals(ArthikaHFT.SIDE_SELL)) {
                                        ClosePositionItem closePositionItem = new ClosePositionItem(positionItem.getSecurity(), positionItem.getAccount(), positionItem.getExposure(), ArthikaHFT.SIDE_BUY);
                                        closePositionItemList.add(closePositionItem);
                                    }
                                }
                            }
                        }
                        if (closePositionItemList.size() > 0) {
                            String alertMessage = context.getString(R.string.close_all_positions_message);
                            int n = 0;
                            for (ClosePositionItem closePositionItem : closePositionItemList) {
                                alertMessage += "\n" + closePositionItem.getSide().toUpperCase() + " " + Utils.doubleToString(closePositionItem.getQuantity()) + " " + closePositionItem.getSecurity() + " " + context.getString(R.string.in) + " " + closePositionItem.getAccount();
                                if (n++ > MAX_MESSAGE_ORDERS) {
                                    alertMessage += "\n...";
                                    break;
                                }
                            }
                            alertClosePosition.setMessage(alertMessage);
                            alertClosePosition.show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            accountPositionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    accountPositionSelected = (String) accountPositionSpinner.getSelectedItem();
                    positionChanged = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            accountAssetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    accountAssetSelected = (String) accountAssetSpinner.getSelectedItem();
                    assetChanged = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            return view;
        }

    }

}
