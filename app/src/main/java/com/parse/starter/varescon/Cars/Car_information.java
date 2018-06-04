package com.parse.starter.varescon.Cars;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.MainActivity;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class Car_information extends Fragment {

    private RecyclerView mHistoryRecyclerView;
    private static CarAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private Button addCarsBtn;
    private static ArrayList resultsHistory = new ArrayList<CarsObject>();
    private int carTypePosition = 0;
    private int carStatusPosition = 0;
    private FirebaseDatabase db;
    private DatabaseReference carsRef;
    private String carType;
    private String carStatus;
    private static TextView noResults;
    private ArrayList<String> carExist;
    private ArrayList<String> prompted;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchText = getView().findViewById(R.id.searchText);
        addCarsBtn = getView().findViewById(R.id.addCarsBtn);
        noResults = getView().findViewById(R.id.noResults);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new CarAdapter(getDataSetHistory(), getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        addCarsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDialog();
            }
        });
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


        try {
            refreshData();
        } catch (Exception e) {
            getCarInfo();
        }

    }

    private void openAddDialog() {
        carExist = new ArrayList<>();
        prompted = new ArrayList<>();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("CAR INFORMATION");
        dialog.setMessage("Add new car information");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View register_layout = inflater.inflate(R.layout.layout_add_cars, null);

        final MaterialEditText editPlate = register_layout.findViewById(R.id.editPlate);
        final MaterialEditText editNo = register_layout.findViewById(R.id.editNo);
        final MaterialSpinner selectCarType = register_layout.findViewById(R.id.selectCarType);
        final MaterialSpinner selectStatus = register_layout.findViewById(R.id.selectStatus);

        selectCarType.setItems("-Type of car -", "TAXI - 4 passenger seats", "Sedan - 4 passenger seats", "Van - 8 passenger seats");
        selectStatus.setItems("-Car status-", "Available", "Not Available");

        selectCarType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carTypePosition = position;
                carType = String.valueOf(item);
            }
        });

        selectStatus.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carStatusPosition = position;
                carStatus = String.valueOf(item);
            }
        });

        InputFilter[] Textfilters = new InputFilter[1];
        Textfilters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {

                    char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

                    for (int index = start; index < end; index++) {
                        if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) {
                            return "";
                        }
                    }
                }
                return null;
            }

        };

        editPlate.setMaxCharacters(3);
        editPlate.setFilters(Textfilters);
        editPlate.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);


        editNo.setMaxCharacters(3);
        //editNo.setFilters(Numbers);
        editNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialog.setView(register_layout);

        dialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editPlate.getText().toString().length() > 3 || editNo.getText().toString().length() > 3) {
                    Snackbar.make(getView(), "Character exceeded to limit!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (editPlate.getText().toString().length() < 3 || editNo.getText().toString().length() < 3) {
                    Snackbar.make(getView(), "Character is less than 3. Please try again", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editPlate.getText().toString()) || TextUtils.isEmpty(editNo.getText().toString())) {
                    Snackbar.make(getView(), "Please Complete the Plate Number", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (carTypePosition == 0) {
                    Snackbar.make(getView(), "Please select the type of car", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (carStatusPosition == 0) {
                    Snackbar.make(getView(), "Please select the status of the car", Snackbar.LENGTH_LONG).show();
                    return;
                }


                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
                userHistoryRef.orderByChild("plate_no").equalTo(editPlate.getText().toString().toUpperCase() + " " + editNo.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Snackbar.make(getView(), "Plate number already exist", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        else{
                            if (!TextUtils.isEmpty(editPlate.getText().toString()) && !TextUtils.isEmpty(editNo.getText().toString()) && carTypePosition != 0 && carStatusPosition != 0) {
                                boolean carStat = false;
                                db = FirebaseDatabase.getInstance();
                                carsRef = db.getReference("Cars");
                                String carId = carsRef.push().getKey();

                                if (carStatus.equals("Available")) {
                                    carStat = true;
                                } else if (carStatus.equals("Not Available")) {
                                    carStat = false;
                                }

                                HashMap map = new HashMap();
                                map.put("plate_no", editPlate.getText().toString().toUpperCase() + " " + editNo.getText().toString());
                                if (carType.equals("TAXI - 4 passenger seats")) {
                                    map.put("type", carType);
                                    map.put("assigned", false);
                                }
                                else
                                    map.put("type", carType);
                                map.put("status", carStat);

                                carsRef.child(carId).updateChildren(map);
                                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Successfully added")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                refreshData();
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                dialog.dismiss();

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private static void getCarInfo() {
        noResults.setVisibility(View.GONE);
        Log.e("sss", "called");
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
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

    private static void FetchRideInfo(String key) {

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        String plate_no = String.valueOf(dataSnapshot.child("plate_no").getValue());
                        String type = String.valueOf(dataSnapshot.child("type").getValue());
                        String status = String.valueOf(dataSnapshot.child("status").getValue());

                        CarsObject obj = new CarsObject(plate_no, type, status, dataSnapshot.getKey());
                        resultsHistory.add(obj);
                        //Collections.reverse(resultsHistory);
                        mHistoryAdapter.notifyDataSetChanged();
                    }
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_car_information, container, false);
    }

    private ArrayList<CarsObject> getDataSetHistory() {
        return resultsHistory;
    }

    public static void refreshData() {
        mHistoryAdapter.clear();
        resultsHistory.clear();
        getCarInfo();
    }
}
