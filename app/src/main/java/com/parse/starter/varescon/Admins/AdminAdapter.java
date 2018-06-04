package com.parse.starter.varescon.Admins;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.parse.starter.varescon.Cars.CarViewHolders;
import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Drivers.DriverAdapter;
import com.parse.starter.varescon.Drivers.DriverObject;
import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by iSwear on 1/7/2018.
 */

public class AdminAdapter extends RecyclerView.Adapter<AdminViewHolders> implements Filterable {
    private List<AdminObject> itemList;
    private List<AdminObject> filteredData;
    private View view;
    private ItemFilter mFilter = new ItemFilter();
    private ArrayList<AdminObject> nList;

    public AdminAdapter(List<AdminObject> itemList, View view) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.view = view;
    }

    @Override
    public AdminViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cars, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        AdminViewHolders rcv = new AdminViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(AdminViewHolders holder, int position) {
        holder.rideIdCar.setText("Admin-ID: " + itemList.get(position).getAdminId() + " "+
                "\nEmail: " + itemList.get(position).getEmail() + " "+
                "\nAdmin Name: " + itemList.get(position).getName()
        );

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
            nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                AdminObject mObject = filteredData.get(i);
                name = mObject.getName();
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
            itemList = (ArrayList<AdminObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
