package com.parse.starter.varescon.Drivers;


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
import com.parse.starter.varescon.Cars.CarAdapter;
import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.MainActivity;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class Driver_information extends Fragment {

    private RecyclerView mHistoryRecyclerView;
    private static DriverAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private Button addDriverBtn;
    private static ArrayList resultsHistory = new ArrayList<DriverObject>();
    private int carTypePosition = 0;
    private int carStatusPosition = 0;
    private FirebaseDatabase db;
    private DatabaseReference carsRef;
    private String carType;
    private String carStatus;
    private FirebaseAuth auth;
    private DatabaseReference users;
    private MaterialSpinner selectAssigned;
    private ArrayList<String> carAvailable;
    private String carSelected;
    private DatabaseReference cardrivers;
    private int carPosition = 0;
    private static TextView noResults;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noResults = getView().findViewById(R.id.noResults);
        searchText = getView().findViewById(R.id.searchText);
        addDriverBtn = getView().findViewById(R.id.addDriverBtn);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.drivers);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new DriverAdapter(getDataSetHistory(), getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        addDriverBtn.setOnClickListener(new View.OnClickListener() {
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
            getDriverInfo();
        }

    }

    private void openAddDialog() {
        carAvailable = new ArrayList<>();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("DRIVER INFORMATION");
        dialog.setMessage("Add a new driver information");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View register_layout = inflater.inflate(R.layout.layout_add_drivers, null);
        dialog.setView(register_layout);
        final MaterialEditText editFirstname = register_layout.findViewById(R.id.editFirstname);
        final MaterialEditText editLastname = register_layout.findViewById(R.id.editLastname);
        final MaterialEditText editEmail = register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);
        selectAssigned = register_layout.findViewById(R.id.selectAssigned);
        selectAssigned.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carSelected = item.toString();
                carPosition = position;
            }
        });

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(d.getKey());
                        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.child("type").getValue().equals("TAXI - 4 passenger seats")) {
                                        if(dataSnapshot.child("assigned").getValue().equals(false)) {
                                            carAvailable.add(dataSnapshot.child("plate_no").getValue() + " " + d.getKey());
                                            selectAssigned.setItems(carAvailable);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

        editFirstname.setFilters(Textfilters);
        editLastname.setFilters(Textfilters);
        editFirstname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editLastname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editPassword.setMinCharacters(6);


        dialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                    Snackbar.make(getView(), "Please enter driver's first name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editLastname.getText().toString())) {
                    Snackbar.make(getView(), "Please enter driver's last name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(getView(), "Please enter driver's email", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(getView(), "Please enter driver's password", Snackbar.LENGTH_LONG).show();
                    return;
                }

                boolean indicator = isEmailValid(editEmail.getText().toString());
                if (!indicator) {
                    Snackbar.make(getView(), "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                }

                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Drivers");
                userHistoryRef.orderByChild("firstname").equalTo(editFirstname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Drivers");
                            userHistoryRef.orderByChild("lastname").equalTo(editLastname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Snackbar.make(getView(), "Driver already exist", Snackbar.LENGTH_LONG).show();
                                        return;
                                    }
                                    else{
                                        final SweetAlertDialog loadingbar = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                                        loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                                        loadingbar.setTitleText("Adding a new driver");
                                        loadingbar.setCancelable(false);
                                        loadingbar.show();
                                        auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                    @Override
                                                    public void onSuccess(AuthResult authResult) {
                                                        //SAVE USER TO DATABASE
                                                        User user = new User();
                                                        user.setEmail(editEmail.getText().toString());
                                                        user.setPassword(editPassword.getText().toString());
                                                        user.setFirstname(editFirstname.getText().toString());
                                                        user.setLastname(editLastname.getText().toString());
                                                        if (carAvailable.size() > 0)
                                                            user.setAssigned(true);
                                                        else
                                                            user.setAssigned(false);
                                                        user.setOnline(false);
                                                        user.setIdentity("0");
                                                        user.setHailed(false);
                                                        user.setProfilePic("https://firebasestorage.googleapis.com/v0/b/varescon-186823.appspot.com/o/Riders%2FprofilePic.png?alt=media&token=d81d10cd-af61-480e-82ec-3d3b717dbf2a");

                                                        final String driverKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                        //ADD TO KEY
                                                        users.child(driverKey)
                                                                .setValue(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        try {
                                                                            String[] car = carAvailable.get(carPosition).split(" ");

                                                                            db = FirebaseDatabase.getInstance();
                                                                            cardrivers = db.getReference("VehicleDrivers");
                                                                            final String requestId = cardrivers.push().getKey();
                                                                            final String plate_no = car[0]+" "+car[1];
                                                                            final String carId = car[2];
                                                                            DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carId);
                                                                            userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (dataSnapshot.exists()) {
                                                                                        HashMap map = new HashMap();
                                                                                        map.put("plate_no", plate_no);
                                                                                        map.put("car", carId);
                                                                                        map.put("driver", driverKey);
                                                                                        map.put("type", dataSnapshot.child("type").getValue().toString());

                                                                                        cardrivers.child(requestId).updateChildren(map);
                                                                                        FirebaseDatabase.getInstance().getReference("Cars").child(carId).child("assigned").setValue(true);
                                                                                        FirebaseDatabase.getInstance().getReference("Drivers").child(driverKey).child("assigned").setValue(true);
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                        }catch (Exception e){
                                                                            FirebaseDatabase.getInstance().getReference("Drivers").child(driverKey).child("assigned").setValue(false);
                                                                        }

                                                                        loadingbar.dismiss();
                                                                        new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                                                                .setTitleText("Success")
                                                                                .setContentText("Successfully added a new driver")
                                                                                .setConfirmText("OK")
                                                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                                    @Override
                                                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                        refreshData();
                                                                                        sweetAlertDialog.dismiss();
                                                                                    }
                                                                                })
                                                                                .show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        loadingbar.dismiss();
                                                                        Snackbar.make(getView(), "Register Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingbar.dismiss();
                                                Snackbar.make(getView(), "Register Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else{
                            final SweetAlertDialog loadingbar = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                            loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                            loadingbar.setTitleText("Adding a new driver");
                            loadingbar.setCancelable(false);
                            loadingbar.show();
                            auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            //SAVE USER TO DATABASE
                                            User user = new User();
                                            user.setEmail(editEmail.getText().toString());
                                            user.setPassword(editPassword.getText().toString());
                                            user.setFirstname(editFirstname.getText().toString());
                                            user.setLastname(editLastname.getText().toString());
                                            if (carAvailable.size() > 0)
                                                user.setAssigned(true);
                                            else
                                                user.setAssigned(false);
                                            user.setOnline(false);
                                            user.setIdentity("0");
                                            user.setHailed(false);
                                            user.setProfilePic("https://firebasestorage.googleapis.com/v0/b/varescon-186823.appspot.com/o/Riders%2FprofilePic.png?alt=media&token=d81d10cd-af61-480e-82ec-3d3b717dbf2a");

                                            final String driverKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            //ADD TO KEY
                                            users.child(driverKey)
                                                    .setValue(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            try {
                                                                String[] car = carAvailable.get(carPosition).split(" ");

                                                                db = FirebaseDatabase.getInstance();
                                                                cardrivers = db.getReference("VehicleDrivers");
                                                                final String requestId = cardrivers.push().getKey();
                                                                final String plate_no = car[0]+" "+car[1];
                                                                final String carId = car[2];
                                                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carId);
                                                                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.exists()) {
                                                                            HashMap map = new HashMap();
                                                                            map.put("plate_no", plate_no);
                                                                            map.put("car", carId);
                                                                            map.put("driver", driverKey);
                                                                            map.put("type", dataSnapshot.child("type").getValue().toString());

                                                                            cardrivers.child(requestId).updateChildren(map);
                                                                            FirebaseDatabase.getInstance().getReference("Cars").child(carId).child("assigned").setValue(true);
                                                                            FirebaseDatabase.getInstance().getReference("Drivers").child(driverKey).child("assigned").setValue(true);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }catch (Exception e){
                                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverKey).child("assigned").setValue(false);
                                                            }

                                                            loadingbar.dismiss();
                                                            new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Success")
                                                                    .setContentText("Successfully added a new driver")
                                                                    .setConfirmText("OK")
                                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                        @Override
                                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                            refreshData();
                                                                            sweetAlertDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            loadingbar.dismiss();
                                                            Snackbar.make(getView(), "Register Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingbar.dismiss();
                                    Snackbar.make(getView(), "Register Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
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

    private static void getDriverInfo() {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Drivers");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchDriverInfo(d.getKey());
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

    private static void FetchDriverInfo(String key) {

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Drivers").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        String driverId = String.valueOf(dataSnapshot.getKey());
                        String driverName = String.valueOf(dataSnapshot.child("firstname").getValue()) + " " + String.valueOf(dataSnapshot.child("lastname").getValue());
                        String driverRating = String.valueOf(dataSnapshot.child("rating").getValue());
                        String assigned;
                        try {
                            assigned = dataSnapshot.child("assigned").getValue().toString();
                        }catch (Exception e){
                            assigned = null;
                        }

                        DriverObject obj = new DriverObject(driverId, driverName, driverRating, assigned);
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
        return inflater.inflate(R.layout.fragment_driver_information, container, false);
    }

    private ArrayList<DriverObject> getDataSetHistory() {
        return resultsHistory;
    }

    public static void refreshData() {
        mHistoryAdapter.clear();
        resultsHistory.clear();
        getDriverInfo();
    }

    public boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            return false;
    }
}
