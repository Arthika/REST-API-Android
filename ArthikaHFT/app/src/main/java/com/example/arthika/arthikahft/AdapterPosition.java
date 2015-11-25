package com.example.arthika.arthikahft;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterPosition extends ArrayAdapter<String> {

    //private Context context;
    //private int layoutResourceId;
    //private List data = new ArrayList();

    public AdapterPosition(Context context, int layoutResourceId, List data) {
        super(context, layoutResourceId, data);
        //this.layoutResourceId = layoutResourceId;
        //this.context = context;
        //this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (position%MainActivity.POSITION_COLUMNS==1) {
            //v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ((TextView) v).setGravity(Gravity.RIGHT);
            //((TextView) v).setWidth(MainActivity.PositionFragment.positionGridView.getColumnWidth());
            ((TextView) v).setWidth(MainActivity.PositionFragment.positionGridView.getWidth()/MainActivity.PositionFragment.positionGridView.getNumColumns());
        }
        if (position%MainActivity.POSITION_COLUMNS==MainActivity.POSITION_COLUMNS-1) {
            //v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            v.setBackgroundResource(R.drawable.buttonborder);
            ((TextView) v).setGravity(Gravity.CENTER);
        }
        return v;
    }

}
