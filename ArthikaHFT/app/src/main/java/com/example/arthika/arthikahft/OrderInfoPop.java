package com.example.arthika.arthikahft;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class OrderInfoPop extends Activity {

    public static String orderInfoOrderIdSelected;
    static TextView orderInfoOrderIdTexView;
    static TextView orderInfoFixIdTexView;
    static TextView orderInfoTInterfaceTexView;
    static TextView orderInfoSecurityTexView;
    static TextView orderInfoQuantityTexView;
    static TextView orderInfoLimitPriceTexView;
    static TextView orderInfoSideTexView;
    static TextView orderInfoTypeTexView;
    static TextView orderInfoFinishedPriceTexView;
    static TextView orderInfoFinishedQuantityTexView;
    static TextView orderInfoComissionTexView;
    static TextView orderInfoStatusTexView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.orderinfo_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        //int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);

        orderInfoOrderIdTexView = (TextView) this.findViewById(R.id.orderInfoOrderIdTexView);
        orderInfoFixIdTexView = (TextView) this.findViewById(R.id.orderInfoFixIdTexView);
        orderInfoTInterfaceTexView = (TextView) this.findViewById(R.id.orderInfoTInterfaceTexView);
        orderInfoSecurityTexView = (TextView) this.findViewById(R.id.orderInfoSecurityTexView);
        orderInfoQuantityTexView = (TextView) this.findViewById(R.id.orderInfoQuantityTexView);
        orderInfoLimitPriceTexView = (TextView) this.findViewById(R.id.orderInfoLimitPriceTexView);
        orderInfoSideTexView = (TextView) this.findViewById(R.id.orderInfoSideTexView);
        orderInfoTypeTexView = (TextView) this.findViewById(R.id.orderInfoTypeTexView);
        orderInfoFinishedPriceTexView = (TextView) this.findViewById(R.id.orderInfoFinishedPriceTexView);
        orderInfoFinishedQuantityTexView = (TextView) this.findViewById(R.id.orderInfoFinishedQuantityTexView);
        orderInfoComissionTexView = (TextView) this.findViewById(R.id.orderInfoComissionTexView);
        orderInfoStatusTexView = (TextView) this.findViewById(R.id.orderInfoStatusTexView);

        boolean found = false;
        for (OrderItem order : MainActivity.closedOrderList){
            if (order.getOrderid().equals(orderInfoOrderIdSelected)){
                fillOrderData(order);
                found = true;
            }
        }
        if (!found){
            for (OrderItem order : MainActivity.pendingOrderList){
                if (order.getOrderid().equals(orderInfoOrderIdSelected)){
                    fillOrderData(order);
                }
            }
        }

        Button orderInfoCloseButton = (Button) this.findViewById(R.id.orderInfoCloseButton);
        orderInfoCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void fillOrderData(OrderItem order){
        orderInfoOrderIdTexView.setText(order.getOrderid());
        orderInfoFixIdTexView.setText(order.getFixid());
        orderInfoTInterfaceTexView.setText(order.getTinterface());
        orderInfoSecurityTexView.setText(order.getSecurity());
        orderInfoQuantityTexView.setText(Utils.intToString(order.getQuantity()));
        if (order.getLimitprice()>0){
            orderInfoLimitPriceTexView.setText(Utils.doubleToString(order.getLimitprice(),order.getPips()));
            orderInfoLimitPriceTexView.setVisibility(View.VISIBLE);
        }
        else{
            orderInfoLimitPriceTexView.setVisibility(View.INVISIBLE);
        }
        orderInfoSideTexView.setText(order.getSide());
        orderInfoTypeTexView.setText(order.getType());
        orderInfoFinishedPriceTexView.setText(Utils.doubleToString(order.getFinishedprice(), order.getPips()));
        orderInfoFinishedQuantityTexView.setText(Utils.intToString(order.getFinishedquantity()));
        if (order.getCommcurrency()!=null){
            orderInfoComissionTexView.setText(Utils.doubleToString(order.getCommission()) + " " + order.getCommcurrency());
        }
        else{
            orderInfoComissionTexView.setText(Utils.doubleToString(order.getCommission()));
        }
        if (order.getReason()!=null){
            orderInfoStatusTexView.setText(order.getStatus() + " (" + order.getReason() + ")");
        }
        else{
            orderInfoStatusTexView.setText(order.getStatus());
        }
    }

}
