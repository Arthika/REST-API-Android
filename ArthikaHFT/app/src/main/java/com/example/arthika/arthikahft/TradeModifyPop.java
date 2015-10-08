package com.example.arthika.arthikahft;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;


/**
 * Created by Jaime on 22/09/2015.
 */
public class TradeModifyPop extends Activity {

    public static String securitySelected;
    public static String fixidSelected;
    public static String side;
    public static String price;
    public static String amount;
    static Spinner tradeModifyAmountSpinner;
    static EditText tradeModifyPriceEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trademodify_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.6));

        TextView tradeModifySecTextView = (TextView) this.findViewById(R.id.tradeModifySecTextView);
        tradeModifySecTextView.setText(side.toUpperCase() + " " + securitySelected);

        ArrayAdapter<String> tradeModifyAmountAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.amountlist);
        tradeModifyAmountSpinner = (Spinner) this.findViewById(R.id.tradeModifyAmountSpinner);
        tradeModifyAmountSpinner.setAdapter(tradeModifyAmountAdapter);
        int spinnerPosition = tradeModifyAmountAdapter.getPosition(amount);
        if (spinnerPosition>=0) {
            tradeModifyAmountSpinner.setSelection(spinnerPosition);
        }
        tradeModifyAmountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                amount = tradeModifyAmountSpinner.getSelectedItem().toString();
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        tradeModifyPriceEditText = (EditText) this.findViewById(R.id.tradeModifyPriceEditText);
        tradeModifyPriceEditText.setText(price);
        tradeModifyPriceEditText.addTextChangedListener(new TextWatcher() {
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

        Button tradeModifyCancelButton = (Button) this.findViewById(R.id.tradeModifyCancelButton);
        tradeModifyCancelButton.setText("CANCEL");
        Button tradeModifyOKButton = (Button) this.findViewById(R.id.tradeModifyOKButton);
        tradeModifyOKButton.setText("MODIFY");

        tradeModifyCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tradeModifyOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyOrder();
            }
        });

    }

    private void refresh(){
        TextView tradeModifyMessageTextView = (TextView) this.findViewById(R.id.tradeModifyMessageTextView);
        tradeModifyMessageTextView.setText("Change order " + side.toUpperCase() + " " + amount + " " + securitySelected + " at " + price);
    }

    private void modifyOrder() {
        new modifyOrderConnection().execute();
    }

    private class modifyOrderConnection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            modifyOrderConnect();
            return null;
        }

    }

    private void modifyOrderConnect() {
        ArthikaHFT.modOrder order = new ArthikaHFT.modOrder();
        order.fixid = fixidSelected;
        order.quantity = Integer.valueOf(amount);
        order.price = Double.parseDouble(tradeModifyPriceEditText.getText().toString());
        try {
            MainActivity.wrapper.modifyOrder(Arrays.asList(order));
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        securitySelected="";
        super.onDestroy();
    }

}
