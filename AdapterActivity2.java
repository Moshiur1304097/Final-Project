package com.example.v.bluetooth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class AdapterActivity2 extends ArrayAdapter<Glucose>{

    public AdapterActivity2(@NonNull Context context, int resource, ArrayList<Glucose> options) {
        super(context, resource, options);
    }
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_view_2,parent,false);
        Glucose current = getItem(position);
        TextView textStop = (TextView)listItemView.findViewById(R.id.list_time);
        textStop.setText(current.getTime());
        TextView textTime = (TextView)listItemView.findViewById(R.id.list_heart);
        textTime.setText(current.getValue());
        return listItemView;
    }
}
