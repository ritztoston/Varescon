package com.parse.starter.varescon.AdminReports;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.parse.starter.varescon.R;

/**
 * Created by iSwear on 1/7/2018.
 */

public class AdminReportViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView riderId;


    public AdminReportViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        riderId = itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View v) {

    }
}
