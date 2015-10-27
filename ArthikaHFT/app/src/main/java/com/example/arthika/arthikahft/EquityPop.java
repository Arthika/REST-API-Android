package com.example.arthika.arthikahft;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by Jaime on 22/09/2015.
 */
public class EquityPop extends Activity {

    public static List<Double> equitystrategylist;
    public static List<Double> equitypoollist;
    public static List<String> equityintervallist;
    private static LineChart equitystrategyChart;
    private static LineChart equitypoolChart;
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

        TextView equityTextView = (TextView) this.findViewById(R.id.equityTextView);
        equityTextView.setText("Equity");

        equityTimeIniTextView = (TextView) this.findViewById(R.id.equityTimeIniTextView);
        equityTimeEndTextView = (TextView) this.findViewById(R.id.equityTimeEndTextView);

        timeIni = "";

        equitystrategyChart = (LineChart) findViewById(R.id.equityStrategyChart);
        equitypoolChart = (LineChart) findViewById(R.id.equityPoolChart);
        myValueFormatter = new MyValueFormatter();

    }

    @Override
    protected void onDestroy() {
        equitystrategyChart =null;
        equitypoolChart =null;
        super.onDestroy();
    }

    public static void refresh(){
        if (equitystrategyChart != null || equitypoolChart != null) {
            if (equitystrategylist == null || equitypoollist == null || equityintervallist == null) {
                return;
            }

            if (equitystrategylist.isEmpty() || equitypoollist.isEmpty() ||equityintervallist.isEmpty() ) {
                return;
            }

            if (timeIni.equals("") && equityTimeIniTextView!=null){
                long timelong = new Double(new Double(equityintervallist.get(0)) * 1000).longValue();
                Date date = new Date();
                date.setTime(timelong);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                equityTimeIniTextView.setText(dateFormat.format(date));
            }

            if (equityTimeEndTextView!=null){
                long timelong = new Double(new Double(equityintervallist.get(equityintervallist.size()-1)) * 1000).longValue();
                Date date = new Date();
                date.setTime(timelong);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                equityTimeEndTextView.setText(dateFormat.format(date));
            }
        }

        if (equitystrategyChart !=null) {
            ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
            for (int i = 0; i < equitystrategylist.size(); i++) {
                Entry entry = new Entry( equitystrategylist.get(i).floatValue(), i);
                valsComp1.add(entry);
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, "Strategy Equity");
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setComp1);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < equityintervallist.size(); i++) {
                xVals.add(equityintervallist.get(i));
            }

            YAxis axis = equitystrategyChart.getAxisLeft();
            axis.setStartAtZero(false);
            axis.setValueFormatter(myValueFormatter);

            equitystrategyChart.getAxisRight().setEnabled(false);
            equitystrategyChart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            data.setDrawValues(false);
            equitystrategyChart.setData(data);
            equitystrategyChart.invalidate();
            equitystrategyChart.setDescription("Strategy Equity");
        }

        if (equitypoolChart !=null) {
            ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
            for (int i = 0; i < equitypoollist.size(); i++) {
                Entry entry = new Entry( equitypoollist.get(i).floatValue(), i);
                valsComp1.add(entry);
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, "Pool Equity");
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setComp1);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < equityintervallist.size(); i++) {
                xVals.add(equityintervallist.get(i));
            }

            YAxis axis = equitypoolChart.getAxisLeft();
            axis.setStartAtZero(false);
            axis.setValueFormatter(myValueFormatter);

            equitypoolChart.getAxisRight().setEnabled(false);
            equitypoolChart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            data.setDrawValues(false);
            equitypoolChart.setData(data);
            equitypoolChart.invalidate();
            equitypoolChart.setDescription("Pool Equity");
        }
    }



    public class MyValueFormatter implements YAxisValueFormatter {

        public MyValueFormatter() {
        }

        private final NavigableMap<Float, String> suffixes = new TreeMap<>();{
            suffixes.put(1000.0f, "K");
            suffixes.put(1000000.0f, "M");
            suffixes.put(1000000000.0f, "G");
            suffixes.put(1000000000000.0f, "T");
            suffixes.put(1000000000000000.0f, "P");
            suffixes.put(1000000000000000000.0f, "E");
        }

        @Override
        public String getFormattedValue(float value, YAxis axis) {
            if (value == Long.MIN_VALUE) return getFormattedValue(Long.MIN_VALUE + 1, axis);
            if (value < 0) return "-" + getFormattedValue(-value, axis);
            if (value < 1000) return String.format("%.2f", value);

            Map.Entry<Float, String> e = suffixes.floorEntry(value);
            Float divideBy = e.getKey();
            String suffix = e.getValue();

            float truncated = value / (divideBy / 10);
            boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
            return hasDecimal ? String.format("%.2f",truncated / 10d) + suffix : String.format("%.2f", truncated / 10) + suffix;
        }
    }

}
