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
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class EquityPop extends Activity {

    public final static List<Double> equityStrategyList = new ArrayList<>();
    public final static List<Double> equityPoolList = new ArrayList<>();
    public final static List<String> intervalList = new ArrayList<>();
    private static LineChart equityStrategyChart;
    private static LineChart equityPoolChart;
    public static String timeIni;
    private static TextView equityTimeIniTextView;
    private static TextView equityTimeEndTextView;
    private static MyValueFormatter myValueFormatter;

    public static final int EQUITY_MAX_VALUES = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.equity_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.8));

        equityTimeIniTextView = (TextView) this.findViewById(R.id.equityTimeIniTextView);
        equityTimeEndTextView = (TextView) this.findViewById(R.id.equityTimeEndTextView);

        timeIni = "";

        equityStrategyChart = (LineChart) findViewById(R.id.equityStrategyChart);
        equityPoolChart = (LineChart) findViewById(R.id.equityPoolChart);
        myValueFormatter = new MyValueFormatter();

        Button equityCloseButton = (Button) this.findViewById(R.id.equityCloseButton);
        equityCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        equityStrategyChart =null;
        equityPoolChart =null;
        super.onDestroy();
    }

    public static void refresh(){
        if (equityStrategyChart != null || equityPoolChart != null) {
            if (equityStrategyList == null || equityPoolList == null || intervalList == null) {
                return;
            }

            if (equityStrategyList.isEmpty() || equityPoolList.isEmpty() ||intervalList.isEmpty() ) {
                return;
            }

            if (timeIni.equals("") && equityTimeIniTextView!=null){
                long timelong = Double.valueOf(Double.valueOf(intervalList.get(0)) * 1000).longValue();
                equityTimeIniTextView.setText(Utils.timeToString(timelong));
            }

            if (equityTimeEndTextView!=null){
                long timelong = Double.valueOf(Double.valueOf(intervalList.get(intervalList.size()-1)) * 1000).longValue();
                equityTimeEndTextView.setText(Utils.timeToString(timelong));
            }
        }

        if (equityStrategyChart !=null) {
            ArrayList<Entry> valsComp1 = new ArrayList<>();
            synchronized(equityStrategyList) {
                for (int i = 0; i < equityStrategyList.size(); i++) {
                    Entry entry = new Entry(equityStrategyList.get(i).floatValue(), i);
                    valsComp1.add(entry);
                }
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, MainActivity.context.getString(R.string.strategy_equity));
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);

            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);

            ArrayList<String> xVals = new ArrayList<>();
            synchronized(intervalList) {
                for (String interval : intervalList) {
                    xVals.add(interval);
                }
            }

            YAxis axis = equityStrategyChart.getAxisLeft();
            axis.setStartAtZero(false);
            axis.setValueFormatter(myValueFormatter);

            equityStrategyChart.getAxisRight().setEnabled(false);
            equityStrategyChart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            data.setDrawValues(false);
            equityStrategyChart.setData(data);
            equityStrategyChart.invalidate();
            equityStrategyChart.setDescription(MainActivity.context.getString(R.string.strategy_equity));
        }

        if (equityPoolChart !=null) {
            ArrayList<Entry> valsComp1 = new ArrayList<>();
            synchronized(equityPoolList) {
                for (int i = 0; i < equityPoolList.size(); i++) {
                    Entry entry = new Entry(equityPoolList.get(i).floatValue(), i);
                    valsComp1.add(entry);
                }
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, MainActivity.context.getString(R.string.pool_equity));
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);

            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setComp1);

            ArrayList<String> xVals = new ArrayList<>();
            synchronized(intervalList) {
                for (String interval : intervalList) {
                    xVals.add(interval);
                }
            }

            YAxis axis = equityPoolChart.getAxisLeft();
            axis.setStartAtZero(false);
            axis.setValueFormatter(myValueFormatter);

            equityPoolChart.getAxisRight().setEnabled(false);
            equityPoolChart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            data.setDrawValues(false);
            equityPoolChart.setData(data);
            equityPoolChart.invalidate();
            equityPoolChart.setDescription(MainActivity.context.getString(R.string.pool_equity));
        }
    }



    public class MyValueFormatter implements YAxisValueFormatter {

        public MyValueFormatter() {
        }

        @Override
        public String getFormattedValue(float value, YAxis axis) {
            return Utils.doubleToString(value);
        }
    }

}
