package com.parse.starter.varescon.DriverReportsRecycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.History.HistoryRecycler.HistoryViewHolders;
import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverReportAdapter extends RecyclerView.Adapter<DriverViewHolders> implements Filterable {
    private List<DriverReportObject> itemList;
    private List<DriverReportObject> filteredData;
    private Context context;
    private ItemFilter mFilter = new ItemFilter();

    public DriverReportAdapter(List<DriverReportObject> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public DriverViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        DriverViewHolders rcv = new DriverViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(DriverViewHolders holder, int position) {
        String earningVal = String.format("%.2f", Float.parseFloat(itemList.get(position).getSalary()));
        holder.riderId.setText("Ride ID: " + itemList.get(position).getRideId() + " "+
                "\nDate: " + itemList.get(position).getDate() +
                "\nCustomer Amount Paid: " + itemList.get(position).getAmt_paid() +
                "\nRide Salary: P " + earningVal
        );

    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
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

    public class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            int count = filteredData.size();
            final ArrayList<DriverReportObject> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                DriverReportObject mObject = filteredData.get(i);
                name = mObject.getDate();
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
            itemList = (ArrayList<DriverReportObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
