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

import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveAdapter extends RecyclerView.Adapter<ReserveViewHolders> implements Filterable{
    private List<ReserveObject> itemList;
    private List<ReserveObject> filteredData;
    private Context context;
    private ReserveAdapter.ItemFilter mFilter = new ReserveAdapter.ItemFilter();

    public ReserveAdapter(List<ReserveObject> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public ReserveViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserve, null, false);

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ReserveViewHolders rcv = new ReserveViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(ReserveViewHolders holder, int position) {
        if (itemList.get(position).getProgress().equals("true"))
            holder.rideIdReserve.setText("Date: " + itemList.get(position).getDate() + " \nTime: " + itemList.get(position).getTime() + " \nMode of Payment: " + itemList.get(position).getMop() + " \nStatus: Done"+" \nRide-ID: "+itemList.get(position).getKey());
        else if(itemList.get(position).getProgress().equals("false")){
            String first = "Date: " + itemList.get(position).getDate() + " \nTime: " + itemList.get(position).getTime() + " \nMode of Payment: " + itemList.get(position).getMop()+" \nStatus: ";
            first = first.replace("\n","<br>");
            String next = "<font color ='#25d062'>On Progress</font>";
            String last = " \nRide-ID: "+itemList.get(position).getKey();
            last = last.replace("\n","<br>");
            holder.rideIdReserve.setText(Html.fromHtml(first+next+last));
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
            final ArrayList<ReserveObject> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                ReserveObject mObject = filteredData.get(i);
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
            itemList = (ArrayList<ReserveObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
