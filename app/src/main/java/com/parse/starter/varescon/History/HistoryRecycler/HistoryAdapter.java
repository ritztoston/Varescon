package com.parse.starter.varescon.History.HistoryRecycler;

import android.content.Context;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.parse.starter.varescon.History.TripHistory;
import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iSwear on 1/7/2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> implements Filterable {
    private List<HistoryObject> itemList;
    private List<HistoryObject> filteredData;
    private Context context;
    private ItemFilter mFilter = new ItemFilter();

    public HistoryAdapter(List<HistoryObject> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        HistoryViewHolders rcv = new HistoryViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolders holder, int position) {
        holder.riderId.setText("Ride Id: " + itemList.get(position).getRideId() + " "+
                "\nDate: " + itemList.get(position).getDate() +
                "\nTime: " + itemList.get(position).getTime() +
                "\nMode of Payment: " + itemList.get(position).getMop()
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

    public class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            int count = filteredData.size();
            final ArrayList<HistoryObject> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                HistoryObject mObject = filteredData.get(i);
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
            itemList = (ArrayList<HistoryObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
