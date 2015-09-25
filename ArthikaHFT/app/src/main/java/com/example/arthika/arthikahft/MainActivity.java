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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    static String[] prices;
    static List<String> secs;
    static String[] TIlist;
    static Integer[] amountlist;
    static String updateTime;
    static AlertDialog alertOrder;
    static Timer timer;
    static MyTimerTask myTimerTask;
    static List<ArthikaHFT.orderTick> orderList;
    static List<String> orderArray;

    private static final int PRICE_COLUMNS = 3;
    private static final int ORDER_COLUMNS = 5;
    private static final int REFRESH_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        wrapper = new ArthikaHFT("http://demo.arthikatrading.com");
        wrapper.doAuthentication("fedenice", "fedenice");

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

        amountlist = new Integer[]{1000, 2000, 5000, 10000};

        TIlist = new String[]{"Baxter_CNX", "Cantor_CNX_3"};

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        sendOrder();
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

        getOrder();

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

    public void a(){
        myTimerTask = new MyTimerTask();
    }


    private void sendOrder() {
        new sendOrderConnection().execute();
    }

    private class sendOrderConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            sendOrderConnect();
            return null;
        }

    }

    private void sendOrderConnect() {
        ArthikaHFT.orderRequest order = new ArthikaHFT.orderRequest();
        order.tinterface = PricesFragment.TISpinner.getSelectedItem().toString();
        order.quantity = (int) PricesFragment.amountSpinner.getSelectedItem();
        if ((PricesFragment.cellSelected % PRICE_COLUMNS)==1) {
            order.security = prices[(PricesFragment.cellSelected-1)];
            order.side = "buy";
        }
        if ((PricesFragment.cellSelected % PRICE_COLUMNS)==2) {
            order.security = prices[(PricesFragment.cellSelected-2)];
            order.side = "sell";
        }
        order.type = "market";

        try {
            wrapper.setOrder(Arrays.asList(order));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getOrder() {
        getOrderConnection conn = new getOrderConnection();
        conn.execute();
    }

    private class getOrderConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            getOrderConnect();
            return null;
        }

    }

    private void getOrderConnect() {
        try {
            orderList = wrapper.getOrder(null, null, null);
            if (orderList==null) {
                orderList = new ArrayList<ArthikaHFT.orderTick>();
            }
            for (int i = 0; i < orderList.size(); i++) {
                ArthikaHFT.orderTick order = orderList.get(i);
                orderArray.add((i + 1) * ORDER_COLUMNS, order.security);
                orderArray.add((i + 1) * ORDER_COLUMNS + 1, String.valueOf(order.quantity));
                orderArray.add((i + 1) * ORDER_COLUMNS + 2,order.side);
                orderArray.add((i + 1) * ORDER_COLUMNS + 3, String.valueOf(order.priceatstart));
                orderArray.add((i + 1) * ORDER_COLUMNS + 4,order.status);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                    final String strDate = simpleDateFormat.format(calendar.getTime());
                    //System.out.println("DATE: " + strDate);
                    if (updateTime!=null && !updateTime.equals("")) {
                        long timelong = new Double(new Double(updateTime) * 1000).longValue();
                        Date date = new Date();
                        date.setTime(timelong);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        PricesFragment.updateTimeTextView.setText(dateFormat.format(date));
                    }
                    ArrayAdapter priceAdapter = (ArrayAdapter) PricesFragment.pricesGridView.getAdapter();
                    priceAdapter.notifyDataSetChanged();
                    PricesFragment.pricesGridView.setAdapter(priceAdapter);

                    ArrayAdapter orderAdapter = (ArrayAdapter) OrderFragment.orderGridView.getAdapter();
                    orderAdapter.notifyDataSetChanged();
                    OrderFragment.orderGridView.setAdapter(orderAdapter);

                    PricePop.refresh();

                }});
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
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
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

        static long id;
        static GridView pricesGridView;
        static TextView updateTimeTextView;
        static Button startButton;
        static Button stopButton;
        static int cellSelected;
        static Spinner amountSpinner;
        static Spinner TISpinner;

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

            pricesGridView = (GridView) view.findViewById(R.id.pricesGridView);

            startButton = (Button) view.findViewById(R.id.startButton);
            startButton.setEnabled(true);

            stopButton = (Button) view.findViewById(R.id.stopButton);
            stopButton.setEnabled(false);

            updateTimeTextView = (TextView) view.findViewById(R.id.updateTimeTextView);
            updateTimeTextView.setText("Click 'Start' for streaming");

            ArrayAdapter priceAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, prices);
            priceAdapter.notifyDataSetChanged();
            pricesGridView.setAdapter(priceAdapter);

            ArrayAdapter<String> TIAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, TIlist);
            TISpinner = (Spinner) view.findViewById(R.id.TISpinner);
            TISpinner.setAdapter(TIAdapter);

            ArrayAdapter<Integer> amountAdapter = new ArrayAdapter<Integer>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, amountlist);
            amountSpinner = (Spinner) view.findViewById(R.id.amountSpinner);
            amountSpinner.setAdapter(amountAdapter);

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startButton.setEnabled(false);
                        stopButton.setEnabled(true);
                        updateTime = "";
                        updateTimeTextView.setText("Getting prices");
                        id = wrapper.getPriceBegin(secs, null, "tob", -1, new ArthikaHFTPriceListenerImp());
                        System.out.println("Starting :" + id);
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
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    updateTimeTextView.setText("Streaming stopped");
                    try {
                        System.out.println("Finishing :" + id);
                        wrapper.getPriceEnd(id);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            pricesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    System.out.println(position);
                    if (position > (PRICE_COLUMNS-1)) {
                        cellSelected = position;
                        if ((cellSelected % PRICE_COLUMNS) == 0) {
                            startActivity(new Intent(v.getContext(), PricePop.class));
                            PricePop.asklist = new ArrayList<Double>();
                            PricePop.bidlist = new ArrayList<Double>();
                            PricePop.intervallist = new ArrayList<String>();
                            PricePop.securitySelected = prices[cellSelected];
                        }
                        String alertMessage = "";
                        if ((cellSelected % PRICE_COLUMNS) == 1) {
                            alertMessage = "BUY " + amountSpinner.getSelectedItem().toString() + " " + prices[(cellSelected - 1)] + " in " + TISpinner.getSelectedItem().toString();
                            alertOrder.setMessage(alertMessage);
                            alertOrder.show();
                        }
                        if ((cellSelected % PRICE_COLUMNS) == 2) {
                            alertMessage = "SELL " + amountSpinner.getSelectedItem().toString() + " " + prices[(cellSelected - 2)] + " in " + TISpinner.getSelectedItem().toString();
                            alertOrder.setMessage(alertMessage);
                            alertOrder.show();
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

        static GridView orderGridView;

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

            orderGridView = (GridView) view.findViewById(R.id.orderGridView);

            orderArray = new ArrayList<String> ();
            orderArray.add("Security");
            orderArray.add("Quantity");
            orderArray.add("Side");
            orderArray.add("Price");
            orderArray.add("Status");

            ArrayAdapter<String> orderAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, orderArray);
            orderAdapter.notifyDataSetChanged();
            orderGridView.setAdapter(orderAdapter);

            return view;
        }

    }


}
