package com.example.arthika.arthikahft;

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
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    static ArthikaHFT wrapper;
    private static String domain;
    private static String user;
    private static String password;
    private static String authentication_port;
    private static String request_port;
    static int width;
    static boolean started;
    static long priceStreamingId;
    static long orderStreamingId;
    static long positionStreamingId;
    static String[] prices;
    static List<String> secs;
    static String[] amountlist;
    static String[] typelist;
    static String[] validitylist;
    static String[] TIlist;
    static String updateTime;
    static AlertDialog alertOrder;
    static Timer timer;
    static MyTimerTask myTimerTask;
    static List<String> pendingOrderArray;
    static List<String> closedOrderArray;
    static List<String> positionArray;
    static List<String> assetArray;

    public static final int DEFAULT_PAD = 16;
    public static final int PRICE_COLUMNS = 3;
    public static final int PENDINGORDER_COLUMNS = 8;
    public static final int CLOSEDORDER_COLUMNS = 6;
    public static final int POSITION_COLUMNS = 4;
    public static final int ASSET_COLUMNS = 2;
    private static final int REFRESH_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        wrapper = new ArthikaHFT(domain, user, password, authentication_port, request_port);
        wrapper.doAuthentication();

        started = false;

        updateTime = "";

        secs = Arrays.asList("EUR_USD", "EUR_GBP", "EUR_JPY", "GBP_JPY", "GBP_USD", "USD_JPY", "AUD_USD", "USD_CAD");
        prices = new String[PRICE_COLUMNS * (secs.size() + 1)];
        prices[0] = "SECURITY";
        prices[1] = "ASK";
        prices[2] = "BID";
        for (int i = 0; i < secs.size(); i++) {
            prices[(i + 1) * PRICE_COLUMNS] = secs.get(i);
            prices[(i + 1) * PRICE_COLUMNS + 1] = "0";
            prices[(i + 1) * PRICE_COLUMNS + 2] = "0";
        }

        amountlist = new String[]{"100000", "200000", "500000", "1000000"};
        typelist = new String[]{"market","limit"};
        validitylist = new String[]{"day", "good till cancel", "inmediate or cancel", "fill or kill", "date", "good for secs", "good for msecs"};
        TIlist = new String[]{"Baxter_CNX", "Cantor_CNX_3"};

        pendingOrderArray = new ArrayList<String> ();
        closedOrderArray = new ArrayList<String> ();
        positionArray = new ArrayList<String> ();
        assetArray = new ArrayList<String> ();

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        cancelOrder();
                    }
                });
        alertBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertOrder = alertBuilder.create();

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getProperties(){
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = getAssets().open("config.properties");
            prop.load(input);
            domain = prop.getProperty("domain");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            authentication_port = prop.getProperty("authentication-port");
            request_port = prop.getProperty("request-port");
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
        String fixid = pendingOrderArray.get(OrderFragment.cellSelected - 6);

        try {
            wrapper.cancelOrder(Arrays.asList(fixid));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                        final String strDate = simpleDateFormat.format(calendar.getTime());
                        if (updateTime != null && !updateTime.equals("")) {
                            long timelong = new Double(new Double(updateTime) * 1000).longValue();
                            Date date = new Date();
                            date.setTime(timelong);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            PricesFragment.updateTimeTextView.setText(dateFormat.format(date));
                        }
                        ArrayAdapter priceAdapter = (ArrayAdapter) PricesFragment.pricesGridView.getAdapter();
                        priceAdapter.notifyDataSetChanged();
                        PricesFragment.pricesGridView.setAdapter(priceAdapter);
                        PricePop.refresh();
                    }

                    if (OrderFragment.pendingOrderGridView!=null) {
                        synchronized(pendingOrderArray) {
                            ArrayAdapter pendingOrderAdapter = (ArrayAdapter) OrderFragment.pendingOrderGridView.getAdapter();
                            pendingOrderAdapter.notifyDataSetChanged();
                            OrderFragment.pendingOrderGridView.setAdapter(pendingOrderAdapter);
                        }

                        synchronized(closedOrderArray) {
                            ArrayAdapter closedOrderAdapter = (ArrayAdapter) OrderFragment.closedOrderGridView.getAdapter();
                            closedOrderAdapter.notifyDataSetChanged();
                            OrderFragment.closedOrderGridView.setAdapter(closedOrderAdapter);
                        }
                    }

                    if (PositionFragment.positionGridView!=null) {
                        synchronized(positionArray) {
                            ArrayAdapter positionAdapter = (ArrayAdapter) PositionFragment.positionGridView.getAdapter();
                            positionAdapter.notifyDataSetChanged();
                            PositionFragment.positionGridView.setAdapter(positionAdapter);
                        }

                        synchronized(assetArray) {
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
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "PRICES";
                case 1:
                    return "ORDERS";
                case 2:
                    return "POSITIONS";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PricesFragment extends Fragment {

        static GridView pricesGridView;
        static TextView updateTimeTextView;
        static Button startButton;
        static Button stopButton;
        static int cellSelected;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PricesFragment newInstance() {
            PricesFragment fragment = new PricesFragment();
            return fragment;
        }

        public PricesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_prices, container, false);
            System.out.println("CREATING PricesFragment");

            pricesGridView = (GridView) view.findViewById(R.id.pricesGridView);

            startButton = (Button) view.findViewById(R.id.startButton);
            stopButton = (Button) view.findViewById(R.id.stopButton);
            updateTimeTextView = (TextView) view.findViewById(R.id.updateTimeTextView);
            if (started){
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
            else{
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                updateTimeTextView.setText("Click 'Start' for streaming");
            }

            ArrayAdapter priceAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, prices);
            priceAdapter.notifyDataSetChanged();
            pricesGridView.setAdapter(priceAdapter);

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    started = true;
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    updateTime = "";
                    updateTimeTextView.setText("Getting prices");
                    try {
                        priceStreamingId = wrapper.getPriceBegin(secs, null, "tob", 1, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + priceStreamingId);
                        orderStreamingId = wrapper.getOrderBegin(null, null, null, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + orderStreamingId);
                        positionStreamingId = wrapper.getPositionBegin(null, null, null, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + positionStreamingId);
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
                    updateTimeTextView.setText("Streaming stopped");
                    try {
                        System.out.println("Finishing :" + priceStreamingId);
                        wrapper.getPriceEnd(priceStreamingId);
                        System.out.println("Finishing :" + orderStreamingId);
                        wrapper.getPriceEnd(orderStreamingId);
                        System.out.println("Finishing :" + positionStreamingId);
                        wrapper.getPriceEnd(positionStreamingId);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            pricesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    if (position > (PRICE_COLUMNS - 1)) {
                        cellSelected = position;
                        if ((cellSelected % PRICE_COLUMNS) == 0) {
                            startActivity(new Intent(v.getContext(), PricePop.class));
                            PricePop.securitySelected = prices[cellSelected];
                        }
                        if ((cellSelected % PRICE_COLUMNS) == 1) {
                            startActivity(new Intent(v.getContext(), TradePop.class));
                            TradePop.securitySelected = prices[cellSelected-1];
                            TradePop.side = "buy";
                            TradePop.price = prices[cellSelected];
                        }
                        if ((cellSelected % PRICE_COLUMNS) == 2) {
                            startActivity(new Intent(v.getContext(), TradePop.class));
                            TradePop.securitySelected = prices[cellSelected-2];
                            TradePop.side = "sell";
                            TradePop.price = prices[cellSelected];
                        }
                    }
                }
            });

            return view;
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
        static int cellSelected;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static OrderFragment newInstance() {
            OrderFragment fragment = new OrderFragment();
            return fragment;
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
            pendingOrderHeaderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2))*2, 0, 0, 0);
            List<String> pendingOrderHeaderArray = new ArrayList<String> ();
            pendingOrderHeaderArray.add("OrderId");
            pendingOrderHeaderArray.add("FixId");
            pendingOrderHeaderArray.add("Security");
            pendingOrderHeaderArray.add("Quantity");
            pendingOrderHeaderArray.add("Side");
            pendingOrderHeaderArray.add("Price");
            pendingOrderHeaderArray.add("Modify");
            pendingOrderHeaderArray.add("Cancel");
            ArrayAdapter<String> pendingOrderHeaderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridviewheader_format, pendingOrderHeaderArray);
            pendingOrderHeaderAdapter.notifyDataSetChanged();
            pendingOrderHeaderGridView.setAdapter(pendingOrderHeaderAdapter);

            pendingOrderGridView = (GridView) view.findViewById(R.id.pendingOrderGridView);
            pendingOrderGridView.setNumColumns(PENDINGORDER_COLUMNS);
            pendingOrderGridView.setPadding(-((width - 6 * DEFAULT_PAD) / (PENDINGORDER_COLUMNS - 2))*2, 0, 0, 0);
            ArrayAdapter<String> pendingOrderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridview_format, pendingOrderArray);
            pendingOrderAdapter.notifyDataSetChanged();
            pendingOrderGridView.setAdapter(pendingOrderAdapter);

            closedOrderHeaderGridView = (GridView) view.findViewById(R.id.closedOrderHeaderGridView);
            closedOrderHeaderGridView.setNumColumns(CLOSEDORDER_COLUMNS);
            closedOrderHeaderGridView.setPadding(-(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1), 0, 0, 0);
            List<String> closedOrderHeaderArray = new ArrayList<String> ();
            closedOrderHeaderArray.add("Id");
            closedOrderHeaderArray.add("Security");
            closedOrderHeaderArray.add("Quantity");
            closedOrderHeaderArray.add("Side");
            closedOrderHeaderArray.add("Price");
            closedOrderHeaderArray.add("Status");
            ArrayAdapter<String> closedOrderHeaderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridviewheader_format, closedOrderHeaderArray);
            closedOrderHeaderAdapter.notifyDataSetChanged();
            closedOrderHeaderGridView.setAdapter(closedOrderHeaderAdapter);

            closedOrderGridView = (GridView) view.findViewById(R.id.closedOrderGridView);
            closedOrderGridView.setNumColumns(CLOSEDORDER_COLUMNS);
            closedOrderGridView.setPadding(-(width - 6 * DEFAULT_PAD) / (CLOSEDORDER_COLUMNS - 1), 0, 0, 0);
            ArrayAdapter<String> closedOrderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridview_format, closedOrderArray);
            closedOrderAdapter.notifyDataSetChanged();
            closedOrderGridView.setAdapter(closedOrderAdapter);

            pendingOrderGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    cellSelected = position;
                    System.out.println("selected " + cellSelected);
                    if ((cellSelected % PENDINGORDER_COLUMNS) == (PENDINGORDER_COLUMNS - 2)) {
                        System.out.println("modify " + pendingOrderArray.get(cellSelected - 5));
                        startActivity(new Intent(v.getContext(), TradeModifyPop.class));
                        TradeModifyPop.fixidSelected = pendingOrderArray.get(cellSelected - 5);
                        TradeModifyPop.securitySelected = pendingOrderArray.get(cellSelected - 4);
                        TradeModifyPop.amount = pendingOrderArray.get(cellSelected - 3);
                        TradeModifyPop.side = pendingOrderArray.get(cellSelected - 2);
                        TradeModifyPop.price = pendingOrderArray.get(cellSelected - 1);
                    }
                    if ((cellSelected % PENDINGORDER_COLUMNS) == (PENDINGORDER_COLUMNS - 1)) {
                        System.out.println("cancel " + pendingOrderArray.get(cellSelected - 6));
                        String alertMessage = "Cancel order?";
                        alertOrder.setMessage(alertMessage);
                        alertOrder.show();
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

        static GridView positionGridView;
        static GridView positionHeaderGridView;
        static GridView assetGridView;
        static GridView assetHeaderGridView;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PositionFragment newInstance() {
            PositionFragment fragment = new PositionFragment();
            return fragment;
        }

        public PositionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_position, container, false);
            System.out.println("CREATING PositionFragment");

            positionHeaderGridView = (GridView) view.findViewById(R.id.positionHeaderGridView);
            positionHeaderGridView.setNumColumns(POSITION_COLUMNS);
            List<String> positionHeaderArray = new ArrayList<String> ();
            positionHeaderArray.add("Security");
            positionHeaderArray.add("Position");
            positionHeaderArray.add("Side");
            positionHeaderArray.add("Avg.Price");
            ArrayAdapter<String> positionHeaderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridviewheader_format, positionHeaderArray);
            positionHeaderAdapter.notifyDataSetChanged();
            positionHeaderGridView.setAdapter(positionHeaderAdapter);

            positionGridView = (GridView) view.findViewById(R.id.positionGridView);
            positionGridView.setNumColumns(POSITION_COLUMNS);
            ArrayAdapter<String> positionAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridview_format, positionArray);
            positionAdapter.notifyDataSetChanged();
            positionGridView.setAdapter(positionAdapter);

            assetHeaderGridView = (GridView) view.findViewById(R.id.assetHeaderGridView);
            assetHeaderGridView.setNumColumns(ASSET_COLUMNS);
            List<String> assetHeaderArray = new ArrayList<String> ();
            assetHeaderArray.add("Currency");
            assetHeaderArray.add("Position");
            ArrayAdapter<String> assetHeaderAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridviewheader_format, assetHeaderArray);
            assetHeaderAdapter.notifyDataSetChanged();
            assetHeaderGridView.setAdapter(assetHeaderAdapter);

            assetGridView = (GridView) view.findViewById(R.id.assetGridView);
            assetGridView.setNumColumns(ASSET_COLUMNS);
            ArrayAdapter<String> assetAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.my_gridview_format, assetArray);
            assetAdapter.notifyDataSetChanged();
            assetGridView.setAdapter(assetAdapter);

            return view;
        }

    }

}
