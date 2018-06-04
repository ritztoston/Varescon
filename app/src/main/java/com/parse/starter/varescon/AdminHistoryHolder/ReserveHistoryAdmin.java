package com.parse.starter.varescon.AdminHistoryHolder;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.parse.starter.varescon.History.HistoryRecycler.ReserveAdapterDriver;
import com.parse.starter.varescon.History.HistoryRecycler.ReserveObjectDriver;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReserveHistoryAdmin extends Fragment {
    private String CustorDriver;
    private static String userId;
    private RecyclerView mReserveRecyclerView;
    private static ReserveAdapterAdmin mReserveAdapter;
    private LinearLayoutManager mReserveLayoutManager;
    private MaterialEditText searchText;
    private static TextView noResults;

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
        searchText = view.findViewById(R.id.searchTextReserve);
        mReserveRecyclerView = view.findViewById(R.id.reserveDoneRecyclerView);
        mReserveRecyclerView.setNestedScrollingEnabled(false);
        mReserveRecyclerView.setHasFixedSize(true);
        mReserveLayoutManager = new LinearLayoutManager(getActivity());
        mReserveLayoutManager.setReverseLayout(true);
        mReserveLayoutManager.setStackFromEnd(true);
        mReserveRecyclerView.setLayoutManager(mReserveLayoutManager);
        mReserveAdapter = new ReserveAdapterAdmin(getDataSetReserve(), getContext());
        mReserveRecyclerView.setAdapter(mReserveAdapter);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                mReserveAdapter.getFilter().filter(s.toString());
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try{
            refreshData();
        }catch (Exception e){
            getUserReserved();
        }

    }

    private static void getUserReserved() {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history);
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

    private static void FetchReserveInfo(String key) {
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
                        ReserveObjectDriver obj = new ReserveObjectDriver(dataSnapshot.getKey(),date, mop, time, progress);
                        resultsReserve.add(obj);
                        mReserveAdapter.notifyDataSetChanged();
                    }
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

    private static ArrayList resultsReserve = new ArrayList<ReserveObjectDriver>();

    private ArrayList<ReserveObjectDriver> getDataSetReserve() {
        return resultsReserve;
    }

    public static void refreshData(){
        mReserveAdapter.clear();
        resultsReserve.clear();
        getUserReserved();
    }
}
