package com.parse.starter.varescon.History.HistoryRecycler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideIdReserve;

    public ReserveViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideIdReserve = itemView.findViewById(R.id.rideIdReserve);

    }

    @Override
    public void onClick(View v) {
        String rawId = rideIdReserve.getText().toString();
        final String[] parts = rawId.split(" ");
        String rideId;
        try{
            rideId = parts[15];
        }catch (Exception e){
            rideId = parts[14];
        }

        Intent intent = new Intent(v.getContext(), HistoryReserveDriverPage.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId);
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}
