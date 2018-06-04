package com.parse.starter.varescon.Drivers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.parse.starter.varescon.Cars.CarViewHolders;
import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverAdapter extends RecyclerView.Adapter<DriverViewHoldersAdmin> implements Filterable {
    private List<DriverObject> itemList;
    private List<DriverObject> filteredData;
    private Context context;
    private ItemFilter mFilter = new ItemFilter();

    public DriverAdapter(List<DriverObject> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public DriverViewHoldersAdmin onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drivers, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        DriverViewHoldersAdmin rcv = new DriverViewHoldersAdmin(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(DriverViewHoldersAdmin holder, int position) {
        Log.e("asd",itemList.get(position).getAssigned());
        holder.rideIdCar.setText("Driver-ID: " + itemList.get(position).getDriverId() + " "+
                "\nDriver's Name: " + itemList.get(position).getDriverName()
        );
        try {
            if(!itemList.get(position).getRating().equals("0")) {
                holder.noRatingsAdmin.setVisibility(View.GONE);
                holder.driverAdminRating.setVisibility(View.VISIBLE);
                holder.driverAdminRating.setRating(Float.parseFloat(String.valueOf(itemList.get(position).getRating())));
            }
            else {
                holder.driverAdminRating.setVisibility(View.GONE);
                holder.noRatingsAdmin.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            holder.driverAdminRating.setVisibility(View.GONE);
            holder.noRatingsAdmin.setVisibility(View.VISIBLE);
        }

        try {
            if(!itemList.get(position).getAssigned().equals("false")) {
                holder.assigned.setVisibility(View.GONE);
            }
            else {
                holder.assigned.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            holder.assigned.setVisibility(View.VISIBLE);
        }
    }

    public void clear() {
        int size = this.itemList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.itemList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            int count = filteredData.size();
            final ArrayList<DriverObject> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                DriverObject mObject = filteredData.get(i);
                name = mObject.getDriverName();
                if (name.toLowerCase().contains(filterString))
                    nList.add(mObject);
            }

            results.values = nList;
            results.count = nList.size();

            Log.e("size",""+nList.size());

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList = (ArrayList<DriverObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
