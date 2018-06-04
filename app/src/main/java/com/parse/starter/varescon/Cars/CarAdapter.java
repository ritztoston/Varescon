package com.parse.starter.varescon.Cars;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class CarAdapter extends RecyclerView.Adapter<CarViewHolders> implements Filterable {
    private List<CarsObject> itemList;
    private List<CarsObject> filteredData;
    private Context context;
    private ItemFilter mFilter = new ItemFilter();

    public CarAdapter(List<CarsObject> itemList, Context context) {
        this.itemList = itemList;
        this.filteredData = itemList;
        this.context = context;
    }

    @Override
    public CarViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cars, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        CarViewHolders rcv = new CarViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(CarViewHolders holder, int position) {
        holder.rideIdCar.setText("Plate Number: " + itemList.get(position).getPlate_no() + " "+
                "\nType: " + itemList.get(position).getType() + " "+
                "\nStatus: " + itemList.get(position).getStatus() +" "+
                "\nCar-ID: " + itemList.get(position).getCarId()
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
            final ArrayList<CarsObject> nList = new ArrayList<>();
            String name;
            for (int i = 0; i < count; i++) {
                CarsObject mObject = filteredData.get(i);
                name = mObject.getPlate_no();
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
            itemList = (ArrayList<CarsObject>) results.values;
            notifyDataSetChanged();
        }
    }
}
