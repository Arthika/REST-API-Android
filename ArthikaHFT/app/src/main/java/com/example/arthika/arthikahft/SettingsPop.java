package com.example.arthika.arthikahft;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * Created by Jaime on 22/09/2015.
 */
public class SettingsPop extends Activity {

    static Spinner domainSpinner;
    static EditText userEditText;
    static EditText passwordEditText;
    static Spinner intervalSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 1), (int) (height * 1));

        ArrayAdapter<String> domainAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.domainlist);
        domainSpinner = (Spinner) this.findViewById(R.id.domainSpinner);
        domainSpinner.setAdapter(domainAdapter);
        int spinnerPosition = domainAdapter.getPosition(MainActivity.domain);
        if (spinnerPosition>=0) {
            domainSpinner.setSelection(spinnerPosition);
        }

        userEditText = (EditText) this.findViewById(R.id.userEditText);
        userEditText.setText(MainActivity.user);

        passwordEditText = (EditText) this.findViewById(R.id.passwordEditText);
        passwordEditText.setText(MainActivity.password);

        ArrayAdapter<Integer> intervalAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.intervallist);
        intervalSpinner = (Spinner) this.findViewById(R.id.intervalSpinner);
        intervalSpinner.setAdapter(intervalAdapter);
        int intervalPosition = intervalAdapter.getPosition(MainActivity.interval);
        if (intervalPosition>=0) {
            intervalSpinner.setSelection(intervalPosition);
        }

        Button tradeModifyCancelButton = (Button) this.findViewById(R.id.settingsCancelButton);
        tradeModifyCancelButton.setText("CANCEL");
        Button tradeModifyOKButton = (Button) this.findViewById(R.id.settingsOKButton);
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
                modifySettings();
                finish();
            }
        });

    }

    private void modifySettings() {
        MainActivity.domain = (String) domainSpinner.getSelectedItem();
        MainActivity.user = userEditText.getText().toString();
        MainActivity.password = passwordEditText.getText().toString();
        MainActivity.interval = (int) intervalSpinner.getSelectedItem();
        MainActivity.refreshSettings();
    }

}
