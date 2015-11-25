package com.example.arthika.arthikahft;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Locale;

public class TradePop extends Activity {

    public static String securitySelected;
    public static String side;
    public static String price;
    public static String amountString;
    public static String ti;
    static Spinner tradeAmountSpinner;
    static Spinner tradeTypeSpinner;
    static Spinner tradeValiditySpinner;
    static Spinner tradeTISpinner;
    static EditText tradePriceEditText;
    static String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trade_pop);

        TextView tradeSecTextView = (TextView) this.findViewById(R.id.tradeSecTextView);
        tradeSecTextView.setText(side.toUpperCase() + " " + securitySelected);

        ArrayAdapter<String> tradeAmountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.amountList);
        tradeAmountSpinner = (Spinner) this.findViewById(R.id.tradeAmountSpinner);
        tradeAmountSpinner.setAdapter(tradeAmountAdapter);
        tradeAmountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                amountString = (String) tradeAmountSpinner.getSelectedItem();
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<String> tradeTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.typeList);
        tradeTypeSpinner = (Spinner) this.findViewById(R.id.tradeTypeSpinner);
        tradeTypeSpinner.setAdapter(tradeTypeAdapter);
        tradeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                type = tradeTypeSpinner.getSelectedItem().toString();
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final ArrayAdapter<String> tradeValidityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.validityList);
        tradeValiditySpinner = (Spinner) this.findViewById(R.id.tradeValiditySpinner);
        tradeValiditySpinner.setAdapter(tradeValidityAdapter);

        ArrayAdapter<String> TIAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.tiList);
        tradeTISpinner = (Spinner) this.findViewById(R.id.tradeTISpinner);
        tradeTISpinner.setAdapter(TIAdapter);
        tradeTISpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ti = tradeTISpinner.getSelectedItem().toString();
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        tradePriceEditText = (EditText) this.findViewById(R.id.tradePriceEditText);
        tradePriceEditText.setText(price);
        tradePriceEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                price = s.toString();
                refresh();
            }
        });

        refresh();

        Button tradeCancelButton = (Button) this.findViewById(R.id.tradeCancelButton);
        tradeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Button tradeOKButton = (Button) this.findViewById(R.id.tradeOKButton);
        tradeOKButton.setText(side.toUpperCase());
        tradeOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tradeOKButton.setEnabled(false);
                sendOrder();
            }
        });

    }

    private void refresh(){
        TextView tradeMessageTextView = (TextView) this.findViewById(R.id.tradeMessageTextView);
        if ("market".equals(type)) {
            tradePriceEditText.setEnabled(false);
            tradeMessageTextView.setText(side.toUpperCase() + " " + amountString + " " + securitySelected + " at market price in " + ti);
        }
        else{
            tradePriceEditText.setEnabled(true);
            tradeMessageTextView.setText(side.toUpperCase() + " " + amountString + " " + securitySelected + " at " + price + " in " + ti);
        }
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
        order.security = securitySelected;
        order.tinterface = ti;
        try {
            order.quantity = Utils.stringToInt(amountString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        order.side = side;
        order.type = type;
        order.timeinforce = tradeValiditySpinner.getSelectedItem().toString();
        if (!order.type.equals(ArthikaHFT.TYPE_MARKET)){
            try {
                NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
                order.price = format.parse(tradePriceEditText.getText().toString()).doubleValue();
            } catch (ParseException e) {
                order.price = Double.parseDouble(tradePriceEditText.getText().toString());
            }
        }
        try {
            MainActivity.wrapper.setOrder(Collections.singletonList(order));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        securitySelected="";
        super.onDestroy();
    }

}
