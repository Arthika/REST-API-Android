package com.example.arthika.arthikahft;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import java.util.List;

public class SettingsPop extends Activity {

    static CheckBoxAdapter secsSelectedAdapter;
    static Spinner domainSpinner;
    static EditText userEditText;
    static EditText passwordEditText;
    static Spinner intervalSpinner;
    static GridView secsSelectedGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_pop);

        /*
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 1), (int) (height * 1));
        */

        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.domainList);
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

        ArrayAdapter<Integer> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.intervalList);
        intervalSpinner = (Spinner) this.findViewById(R.id.intervalSpinner);
        intervalSpinner.setAdapter(intervalAdapter);
        int intervalPosition = intervalAdapter.getPosition(MainActivity.interval);
        if (intervalPosition>=0) {
            intervalSpinner.setSelection(intervalPosition);
        }

        secsSelectedGridView = (GridView) this.findViewById(R.id.secsSelectedGridView);
        secsSelectedGridView.setNumColumns(3);
        secsSelectedAdapter = new CheckBoxAdapter(this, R.layout.my_checkbox_format, MainActivity.secsAll, MainActivity.secsSelected);
        secsSelectedAdapter.notifyDataSetChanged();
        secsSelectedGridView.setAdapter(secsSelectedAdapter);

        Button settingsCancelButton = (Button) this.findViewById(R.id.settingsCancelButton);
        settingsCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button settingsOKButton = (Button) this.findViewById(R.id.settingsOKButton);
        settingsOKButton.setOnClickListener(new View.OnClickListener() {
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
        SparseBooleanArray res = secsSelectedAdapter.mCheckStates;
        for (int i=0; i<res.size(); i++){
            MainActivity.secsSelected.set(i,res.get(i));
        }
        MainActivity.ssl = MainActivity.domain.equals(MainActivity.domainList[0]);
        MainActivity.refreshSettings();
    }

    private static class CheckBoxAdapter extends ArrayAdapter<String> implements CompoundButton.OnCheckedChangeListener {

        public SparseBooleanArray mCheckStates;

        public CheckBoxAdapter(Context context, int resource, String[] objects, List<Boolean> secSelected) {
            super(context, resource, objects);
            mCheckStates = new SparseBooleanArray(objects.length);
            for (int i=0; i<objects.length; i++){
                mCheckStates.put(i,secSelected.get(i));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CheckBox view = (CheckBox) super.getView(position, convertView, parent);
            view.setTag(position);
            view.setChecked(mCheckStates.get(position, false));
            view.setOnCheckedChangeListener(this);
            return view;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mCheckStates.put((Integer) buttonView.getTag(), isChecked);
        }
    }

}
