package com.example.arthika.arthikahft;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AdapterPrices extends ArrayAdapter<String> {

    private Context context;
    //private int layoutResourceId;
    //private String[] data = new String[0];

    public AdapterPrices(Context context, int layoutResourceId, String[] data) {
        super(context, layoutResourceId, data);
        //this.layoutResourceId = layoutResourceId;
        this.context = context;
        //this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ((TextView) v).setGravity(Gravity.CENTER);
        if (position>=MainActivity.PRICE_COLUMNS) {
            v.setBackgroundResource(R.drawable.buttonborder2);
        }
        else{
            Spannable clickText;
            if (position==1){
                clickText = new SpannableString(context.getString(R.string.click_buy));
            }
            else if (position==2){
                clickText = new SpannableString(context.getString(R.string.click_sell));
            }
            else{
                clickText = new SpannableString(context.getString(R.string.click_chart));
            }
            clickText.setSpan(new StyleSpan(Typeface.NORMAL), 0, clickText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            clickText.setSpan(new RelativeSizeSpan(0.6f), 0, clickText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) v).append("\n");
            ((TextView) v).append(clickText);

        }
        return v;
    }

}
