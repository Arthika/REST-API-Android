package com.example.arthika.arthikahft;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class PricePop extends Activity {

    public static final List<Double> askList = new ArrayList<>();
    public static final List<Double> bidList = new ArrayList<>();
    public static final List<String> intervalList = new ArrayList<>();
    public static String securitySelected;
    private static LineChart priceChart;
    private static String timeIni;
    private static TextView priceTimeIniTextView;
    private static TextView priceTimeEndTextView;

    private static final int PRICE_MAX_VALUES = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.price_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.8));

        TextView popSecTextView = (TextView) this.findViewById(R.id.popSecTextView);
        popSecTextView.setText(securitySelected);

        priceTimeIniTextView = (TextView) this.findViewById(R.id.priceTimeIniTextView);
        priceTimeEndTextView = (TextView) this.findViewById(R.id.priceTimeEndTextView);

        askList.clear();
        bidList.clear();
        intervalList.clear();
        timeIni = "";

        priceChart = (LineChart) findViewById(R.id.priceChart);

        Button priceCloseButton = (Button) this.findViewById(R.id.priceCloseButton);
        priceCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        priceChart=null;
        securitySelected="";
        super.onDestroy();
    }

    public static void refresh(){
        if (priceChart!=null) {

            if (askList == null || bidList == null || intervalList == null) {
                return;
            }

            if (askList.isEmpty() || bidList.isEmpty() || intervalList.isEmpty() ) {
                return;
            }

            if (timeIni.equals("") && priceTimeIniTextView!=null){
                long timelong = Double.valueOf(Double.valueOf(intervalList.get(0)) * 1000).longValue();
                priceTimeIniTextView.setText(Utils.timeToString(timelong));
            }

            if (priceTimeEndTextView!=null){
                long timelong = Double.valueOf(Double.valueOf(intervalList.get(intervalList.size()-1)) * 1000).longValue();
                priceTimeEndTextView.setText(Utils.timeToString(timelong));
            }

            if (intervalList.size() > PRICE_MAX_VALUES){
                int clearValues = intervalList.size() - PRICE_MAX_VALUES;
                synchronized(askList) {
                    for (int i = 0; i < clearValues; i++) {
                        askList.remove(0);
                    }
                }
                synchronized(bidList) {
                    for (int i = 0; i < clearValues; i++) {
                        bidList.remove(0);
                    }
                }
                synchronized(intervalList) {
                    for (int i = 0; i < clearValues; i++) {
                        intervalList.remove(0);
                    }
                    long timelong = Double.valueOf(Double.valueOf(intervalList.get(0)) * 1000).longValue();
                    priceTimeIniTextView.setText(Utils.timeToString(timelong));
                }
            }

            ArrayList<Entry> valsComp1 = new ArrayList<>();
            ArrayList<Entry> valsComp2 = new ArrayList<>();
            synchronized(askList) {
                for (int i = 0; i < askList.size(); i++) {
                    Entry entry = new Entry(askList.get(i).floatValue(), i);
                    valsComp1.add(entry);
                }
            }
            synchronized(bidList) {
                for (int i = 0; i < bidList.size(); i++) {
                    Entry entry = new Entry(bidList.get(i).floatValue(), i);
                    valsComp2.add(entry);
                }
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, ArthikaHFT.SIDE_ASK.toUpperCase());
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);
            LineDataSet setComp2 = new LineDataSet(valsComp2, ArthikaHFT.SIDE_BID.toUpperCase());
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp2.setCircleColor(Color.RED);
            setComp2.setColor(Color.RED);

            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);
            dataSets.add(setComp2);

            ArrayList<String> xVals = new ArrayList<>();
            synchronized(intervalList) {
                for (String interval : intervalList) {
                    xVals.add(interval);
                }
            }

            YAxis axis = priceChart.getAxisLeft();
            axis.setStartAtZero(false);

            priceChart.getAxisRight().setEnabled(false);
            priceChart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            priceChart.setData(data);
            priceChart.invalidate();
            priceChart.setDescription(securitySelected);
        }
    }

}
