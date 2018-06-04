package com.parse.starter.varescon.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.parse.starter.varescon.R;

/**
 * Created by iSwear on 12/6/2017.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{

    View myView;

    public CustomInfoWindow(Context context) {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.custom_rider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView textPickupTitle = (myView.findViewById(R.id.textpickupInfo));
        textPickupTitle.setText(marker.getTitle());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
