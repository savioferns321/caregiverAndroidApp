package com.sjsu.caregivergeofencesample.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sjsu.caregivergeofencesample.R;
import com.sjsu.caregivergeofencesample.model.GeoFenceDetail;

import java.util.List;

/**
 * Custom adapter for storing patient names.
 * Created by savioubuntu on 4/22/17.
 */

public class PatientArrayAdapter extends ArrayAdapter<GeoFenceDetail> {


    public PatientArrayAdapter(@NonNull Context context, @LayoutRes int resource,
                               @NonNull List<GeoFenceDetail> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GeoFenceDetail detail = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.geofence_detail_row,
                    parent, false);
        }
        TextView detailView = (TextView)convertView.findViewById(R.id.detail_row);

        detailView.setText(detail.getFirstName()+" "+detail.getLastName());

        return detailView;
    }
}
