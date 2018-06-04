package com.parse.starter.varescon.History.HistoryRecycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveAdapterDriver extends RecyclerView.Adapter<ReserveViewHoldersDriver> implements Filterable{
    private List<ReserveObjectDriver> itemList;
    private List<ReserveObjectDriver> filteredData;
    private Context context;
    private ReserveAdapterDriver.ItemFilter mFilter = new ReserveAdapterDriver.ItemFilter();

    public ReserveAdapterDriver(List<ReserveObjectDriver> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public ReserveViewHoldersDriver onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserve, null, false);

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ReserveViewHoldersDriver rcv = new ReserveViewHoldersDriver(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(ReserveViewHoldersDriver holder, int position) {
        if (itemList.get(position).getProgress().equals("true"))
            holder.rideIdReserve.setText("Ride-ID: "+itemList.get(position).getRideId()+" \nDate: " + itemList.get(position).getDate() + "\nTime: " + itemList.get(position).getTime() + "\nMode of Payment: " + itemList.get(position).getMop() + "\nStatus: Done");
        else if(itemList.get(position).getProgress().equals("false")){

            String first = "Ride-ID: "+itemList.get(position).getRideId()+" \nDate: " + itemList.get(position).getDate() + "\nTime: " + itemList.get(position).getTime() + "\nMode of Payment: " + itemList.get(position).getMop()+"\nStatus: ";
            first = first.replace("\n","<br>");
            String next = "<font color ='#25d062'>On Progress</font>";
            holder.rideIdReserve.setText(Html.fromHtml(first+next));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
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
    public Filter getFilter() {
        return mFilter;
    }

    public class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            int count = filteredData.size();
            final ArrayList<ReserveObjectDriver> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                ReserveObjectDriver mObject = filteredData.get(i);
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
            itemList = (ArrayList<ReserveObjectDriver>) results.values;
            notifyDataSetChanged();
        }
    }
}
