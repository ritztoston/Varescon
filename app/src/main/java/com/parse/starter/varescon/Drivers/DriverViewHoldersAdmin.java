package com.parse.starter.varescon.Drivers;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Cars.Car_information;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.parse.starter.varescon.Drivers.Driver_information.refreshData;

/**
 * Created by iSwear on 1/7/2018.
 */

public class DriverViewHoldersAdmin extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideIdCar, noRatingsAdmin, assigned;
    public MaterialRatingBar driverAdminRating;
    public MaterialSpinner selectAssigned;
    private String carSelected;
    private ArrayList<String> carAvailable;
    private FirebaseDatabase db;
    private DatabaseReference cardrivers;
    private int assignPos = 0;
    private String originalFname, originalLname;
    private int i;


    public DriverViewHoldersAdmin(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);


        selectAssigned = itemView.findViewById(R.id.selectAssigned);
        assigned = itemView.findViewById(R.id.assigned);
        rideIdCar = itemView.findViewById(R.id.rideIdCar);
        noRatingsAdmin = itemView.findViewById(R.id.noRatingsAdmin);
        driverAdminRating = itemView.findViewById(R.id.driverAdminRating);
        driverAdminRating.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        carAvailable = new ArrayList<>();
        carAvailable.add("-Assign Driver-");
        String[] split = rideIdCar.getText().toString().split(" ");
        final String driverID = split[1];


        AlertDialog.Builder dialog = new AlertDialog.Builder(itemView.getContext());
        dialog.setTitle("DRIVER INFORMATION");
        dialog.setMessage("Edit a new driver information");

        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View register_layout = inflater.inflate(R.layout.layout_add_drivers_edit, null);

        final TextView assignedTo = register_layout.findViewById(R.id.assignedTo);
        final MaterialEditText editFirstname = register_layout.findViewById(R.id.editFirstname);
        final MaterialEditText editLastname = register_layout.findViewById(R.id.editLastname);
        selectAssigned = register_layout.findViewById(R.id.selectAssigned);
        selectAssigned.setVisibility(View.VISIBLE);

        selectAssigned.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carSelected = item.toString();
                assignPos = position;
            }
        });

        DatabaseReference cars = FirebaseDatabase.getInstance().getReference("Cars");
        cars.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        if (dataSnapshot.child("assigned").getValue().equals(false)) {
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
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Drivers").child(driverID);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        editFirstname.setText(dataSnapshot.child("firstname").getValue().toString());
                        editLastname.setText(dataSnapshot.child("lastname").getValue().toString());
                        if (dataSnapshot.child("assigned").getValue().equals(true)) {
                            DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("VehicleDrivers");
                            userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                            DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("VehicleDrivers").child(d.getKey());
                                            userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        if (dataSnapshot.child("type").getValue().equals("TAXI - 4 passenger seats")) {
                                                            if (dataSnapshot.child("driver").getValue().equals(driverID)) {
                                                                assignedTo.setText("Currently assigned to plate no.: " + dataSnapshot.child("plate_no").getValue());
                                                                selectAssigned.setVisibility(View.GONE);
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
                            selectAssigned.setItems();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        i = 0;
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                i++;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
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
        editFirstname.addTextChangedListener(tw);
        editLastname.addTextChangedListener(tw);

        dialog.setView(register_layout);
        dialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                    Snackbar.make(itemView, "Please enter driver's first name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editLastname.getText().toString())) {
                    Snackbar.make(itemView, "Please enter driver's last name", Snackbar.LENGTH_LONG).show();
                    return;
                }

                final SweetAlertDialog loadingbar = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                loadingbar.setTitleText("Adding a new driver");
                loadingbar.setCancelable(false);
                loadingbar.show();


                if(i > 2) {
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
                                            Snackbar.make(itemView, "Driver already exist", Snackbar.LENGTH_LONG).show();
                                            loadingbar.dismiss();
                                            return;
                                        } else {
                                            if (assignPos == 0) {
                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());
                                                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Success")
                                                        .setContentText("Successfully edited a driver")
                                                        .setConfirmText("OK")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                refreshData();
                                                                sweetAlertDialog.dismiss();
                                                            }
                                                        })
                                                        .show();
                                                loadingbar.dismiss();
                                            } else if (assignPos > 0) {
                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());

                                                String[] split = carAvailable.get(assignPos).split(" ");
                                                final String plate_no = split[0] + " " + split[1];
                                                final String carId = split[2];

                                                db = FirebaseDatabase.getInstance();
                                                cardrivers = db.getReference("VehicleDrivers");
                                                final String requestId = cardrivers.push().getKey();

                                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carId);
                                                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            HashMap map = new HashMap();
                                                            map.put("plate_no", plate_no);
                                                            map.put("car", carId);
                                                            map.put("driver", driverID);
                                                            map.put("type", dataSnapshot.child("type").getValue().toString());

                                                            cardrivers.child(requestId).updateChildren(map);
                                                            FirebaseDatabase.getInstance().getReference("Cars").child(carId).child("assigned").setValue(true);
                                                            FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("assigned").setValue(true);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Success")
                                                        .setContentText("Successfully edited a driver")
                                                        .setConfirmText("OK")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                refreshData();
                                                                sweetAlertDialog.dismiss();
                                                            }
                                                        })
                                                        .show();
                                                loadingbar.dismiss();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                if (assignPos == 0) {
                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());
                                    new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Successfully edited a driver")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    refreshData();
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                    loadingbar.dismiss();
                                } else if (assignPos > 0) {
                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());

                                    String[] split = carAvailable.get(assignPos).split(" ");
                                    final String plate_no = split[0] + " " + split[1];
                                    final String carId = split[2];

                                    db = FirebaseDatabase.getInstance();
                                    cardrivers = db.getReference("VehicleDrivers");
                                    final String requestId = cardrivers.push().getKey();

                                    DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carId);
                                    userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                HashMap map = new HashMap();
                                                map.put("plate_no", plate_no);
                                                map.put("car", carId);
                                                map.put("driver", driverID);
                                                map.put("type", dataSnapshot.child("type").getValue().toString());

                                                cardrivers.child(requestId).updateChildren(map);
                                                FirebaseDatabase.getInstance().getReference("Cars").child(carId).child("assigned").setValue(true);
                                                FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("assigned").setValue(true);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Successfully edited a driver")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    refreshData();
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                    loadingbar.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else{
                    if (assignPos == 0) {
                        FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                        FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());
                        new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Successfully edited a driver")
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        refreshData();
                                        sweetAlertDialog.dismiss();
                                    }
                                })
                                .show();
                        loadingbar.dismiss();
                    } else if (assignPos > 0) {
                        FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("firstname").setValue(editFirstname.getText().toString());
                        FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("lastname").setValue(editLastname.getText().toString());

                        String[] split = carAvailable.get(assignPos).split(" ");
                        final String plate_no = split[0] + " " + split[1];
                        final String carId = split[2];

                        db = FirebaseDatabase.getInstance();
                        cardrivers = db.getReference("VehicleDrivers");
                        final String requestId = cardrivers.push().getKey();

                        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carId);
                        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    HashMap map = new HashMap();
                                    map.put("plate_no", plate_no);
                                    map.put("car", carId);
                                    map.put("driver", driverID);
                                    map.put("type", dataSnapshot.child("type").getValue().toString());

                                    cardrivers.child(requestId).updateChildren(map);
                                    FirebaseDatabase.getInstance().getReference("Cars").child(carId).child("assigned").setValue(true);
                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverID).child("assigned").setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Successfully edited a driver")
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        refreshData();
                                        sweetAlertDialog.dismiss();
                                    }
                                })
                                .show();
                        loadingbar.dismiss();
                    }
                }



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

}
