package com.parse.starter.varescon.History;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.parse.starter.varescon.History.HistoryRecycler.ReserveAdapter;
import com.parse.starter.varescon.History.HistoryRecycler.ReserveObject;
import com.parse.starter.varescon.R;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReserveHistory extends Fragment {
    private String CustorDriver, userId;
    private RecyclerView mReserveRecyclerView;
    private ReserveAdapter mReserveAdapter;
    private LinearLayoutManager mReserveLayoutManager;
    private TextView noResults;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null)
            CustorDriver = bundle.getString("CustorDriver");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noResults = view.findViewById(R.id.noResults);
        mReserveRecyclerView = view.findViewById(R.id.reserveDoneRecyclerView);
        mReserveRecyclerView.setNestedScrollingEnabled(false);
        mReserveRecyclerView.setHasFixedSize(true);
        mReserveLayoutManager = new LinearLayoutManager(getActivity());
        mReserveLayoutManager.setReverseLayout(true);
        mReserveLayoutManager.setStackFromEnd(true);
        mReserveRecyclerView.setLayoutManager(mReserveLayoutManager);
        mReserveAdapter = new ReserveAdapter(getDataSetReserve(), getContext());
        mReserveRecyclerView.setAdapter(mReserveAdapter);


        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserReserved();

    }

    private void getUserReserved() {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.riders).child(userId).child(Common.historychild);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchReserveInfo(d.getKey());
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

    private void FetchReserveInfo(final String key) {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("History").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.child("type").getValue().equals("reserve")) {
                        String date = String.valueOf(dataSnapshot.child("date").getValue());
                        String mop = String.valueOf(dataSnapshot.child("mode_of_payment").getValue());
                        String time = String.valueOf(dataSnapshot.child("time").getValue());
                        String progress = String.valueOf(dataSnapshot.child("progress").getValue());
                        ReserveObject obj = new ReserveObject(date, mop, time, progress, key);
                        resultsReserve.add(obj);
                        mReserveAdapter.notifyDataSetChanged();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserve_history, container, false);
    }

    private ArrayList resultsReserve = new ArrayList<ReserveObject>();

    private ArrayList<ReserveObject> getDataSetReserve() {
        return resultsReserve;
    }
}
