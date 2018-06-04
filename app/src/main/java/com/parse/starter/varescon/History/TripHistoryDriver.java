package com.parse.starter.varescon.History;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.History.HistoryRecycler.HistoryAdapter;
import com.parse.starter.varescon.History.HistoryRecycler.HistoryObject;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripHistoryDriver extends Fragment {
    private String CustorDriver, userId;
    private RecyclerView mHistoryRecyclerView;
    private HistoryAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private TextView noResults;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noResults = view.findViewById(R.id.noResults);
        searchText = view.findViewById(R.id.searchText);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e("userId", userId);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                mHistoryAdapter.getFilter().filter(s.toString());
            }
        });


        getUserHistoryId();
    }


    private void getUserHistoryId() {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(userId).child(Common.historychild);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchRideInfo(d.getKey());
                    }

                }
                else{
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInfo(String key) {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(key);
        userHistoryRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue().equals("regular")) {
                        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(String.valueOf(dataSnapshot.child("driver").getValue()));
                        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot d) {
                                if (d.exists()) {
                                    String firstname = String.valueOf(d.child("firstname").getValue());
                                    String lastname = String.valueOf(d.child("lastname").getValue());
                                    String name = firstname + " " + lastname;

                                    String rideId = String.valueOf(dataSnapshot.getKey());
                                    String date = String.valueOf(dataSnapshot.child("date").getValue());
                                    String time = String.valueOf(dataSnapshot.child("time").getValue());
                                    String amount = String.valueOf(dataSnapshot.child("amount").getValue());
                                    String mop = String.valueOf(dataSnapshot.child("mode_of_payment").getValue());
                                    String pickupLocation = String.valueOf(dataSnapshot.child("pickup_location").getValue());
                                    String destination = String.valueOf(dataSnapshot.child("destination").getValue());
                                    String rating = String.valueOf(dataSnapshot.child("rating").getValue());
                                    HistoryObject obj = new HistoryObject(rideId ,date, time, mop, destination, pickupLocation, rating, name, amount);
                                    resultsHistory.add(obj);
                                    //Collections.reverse(resultsHistory);
                                    mHistoryAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else{
                    noResults.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_history, container, false);
    }


    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }
}
