package com.parse.starter.varescon.DriverReportsRecycler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.parse.starter.varescon.History.HistoryRecycler.HistorySinglePage;
import com.parse.starter.varescon.R;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView riderId;


    public DriverViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        riderId = itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View v) {

    }
}
