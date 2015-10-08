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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jaime on 22/09/2015.
 */
public class PricePop extends Activity {

    public static List<Double> asklist;
    public static List<Double> bidlist;
    public static List<String> intervallist;
    public static String securitySelected;
    private static LineChart chart;
    private static String timeIni;
    private static TextView timeIniTextView;
    private static TextView timeEndTextView;

    private static final int MAX_VALUES = 80;


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

        timeIniTextView = (TextView) this.findViewById(R.id.timeIniTextView);
        timeEndTextView = (TextView) this.findViewById(R.id.timeEndTextView);

        asklist = new ArrayList<Double>();
        bidlist = new ArrayList<Double>();
        intervallist = new ArrayList<String>();
        timeIni = "";

        chart = (LineChart) findViewById(R.id.chart);

    }

    @Override
    protected void onDestroy() {
        chart=null;
        securitySelected="";
        super.onDestroy();
    }

    public static void refresh(){
        if (chart!=null) {
            ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
            ArrayList<Entry> valsComp2 = new ArrayList<Entry>();

            if (asklist == null || bidlist == null || intervallist == null) {
                return;
            }

            if (asklist.isEmpty() || bidlist.isEmpty() || intervallist.isEmpty() ) {
                return;
            }

            if (timeIni.equals("") && timeIniTextView!=null){
                long timelong = new Double(new Double(intervallist.get(0)) * 1000).longValue();
                Date date = new Date();
                date.setTime(timelong);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                timeIniTextView.setText(dateFormat.format(date));
            }

            if (timeEndTextView!=null){
                long timelong = new Double(new Double(intervallist.get(intervallist.size()-1)) * 1000).longValue();
                Date date = new Date();
                date.setTime(timelong);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                timeEndTextView.setText(dateFormat.format(date));
            }

            if (intervallist.size()>MAX_VALUES){
                synchronized(asklist){
                    for (int i=0; i<10; i++){
                        asklist.remove(0);
                    }
                    long timelong = new Double(new Double(intervallist.get(0)) * 1000).longValue();
                    Date date = new Date();
                    date.setTime(timelong);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                    timeIniTextView.setText(dateFormat.format(date));
                }
                synchronized(bidlist){
                    for (int i=0; i<10; i++){
                        bidlist.remove(0);
                    }
                }
                synchronized(intervallist){
                    for (int i=0; i<10; i++){
                        intervallist.remove(0);
                    }
                }
            }

            for (int i = 0; i < asklist.size(); i++) {
                Entry entry = new Entry( asklist.get(i).floatValue(), i);
                valsComp1.add(entry);
            }
            for (int i = 0; i < bidlist.size(); i++) {
                Entry entry = new Entry( bidlist.get(i).floatValue(), i);
                valsComp2.add(entry);
            }

            LineDataSet setComp1 = new LineDataSet(valsComp1, "ASK");
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp1.setCircleColor(Color.BLUE);
            setComp1.setColor(Color.BLUE);
            LineDataSet setComp2 = new LineDataSet(valsComp2, "BID");
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
            setComp2.setCircleColor(Color.RED);
            setComp2.setColor(Color.RED);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setComp1);
            dataSets.add(setComp2);

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < intervallist.size(); i++) {
                xVals.add(intervallist.get(i));
            }

            YAxis axis = chart.getAxisLeft();
            axis.setStartAtZero(false);

            chart.getAxisRight().setEnabled(false);
            chart.getXAxis().setEnabled(false);

            LineData data = new LineData(xVals, dataSets);
            chart.setData(data);
            chart.invalidate();

        }
    }

}
