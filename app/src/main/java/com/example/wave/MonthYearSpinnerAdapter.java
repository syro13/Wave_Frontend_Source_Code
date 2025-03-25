package com.example.wave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MonthYearSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> months;

    public MonthYearSpinnerAdapter(Context context, List<String> months) {
        super(context, R.layout.month_year_spinner_item, months);
        this.context = context;
        this.months = months;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.month_year_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_text);

        textView.setText(months.get(position));
       // Ensures icon is visible in the selected view

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.month_year_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_text);
        textView.setText(months.get(position));

        return convertView;
    }
}
