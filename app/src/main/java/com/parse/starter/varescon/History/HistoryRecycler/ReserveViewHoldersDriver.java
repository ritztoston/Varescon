package com.parse.starter.varescon.History.HistoryRecycler;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Cars.Car_information;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.History.ReserveHistoryDriver;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.RiderActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by iSwear on 1/7/2018.
 */

public class ReserveViewHoldersDriver extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideIdReserve;
    private String plate;
    private ArrayList<String> driverKeyNotAvail;
    private ArrayList<String> driverKeyAvailable;
    MaterialSpinner selectDriver;
    String dateSet;
    String itemSplit;
    private int itemPosition = 0;
    private String date;
    private String status;

    public ReserveViewHoldersDriver(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideIdReserve = itemView.findViewById(R.id.rideIdReserve);

    }

    @Override
    public void onClick(View v) {
        String rawId = rideIdReserve.getText().toString();
        final String[] parts = rawId.split(" ");
        final String rideId = parts[1];
        date = parts[3] + " " + parts[4] + " " + parts[5];
        try {
            status = parts[11] + " " + parts[12];
        } catch (Exception e) {
            status = parts[11];
        }

        Date date1, date2;
        Long differenceDate = Long.valueOf(0);
        String dayDifference;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = df.format(c.getTime());
        try {
            date1 = df.parse(formattedDate);
            date2 = df.parse(date);

            Long difference = Math.abs(date1.getTime() - date2.getTime());
            differenceDate = (difference / (24 * 60 * 60 * 1000));

            dayDifference = Long.toString(differenceDate);
            Log.e("asd", dayDifference);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (differenceDate > 2) {
            if (status.equals("Done")) {
                Intent intent = new Intent(itemView.getContext(), HistoryReserveDriverPage.class);
                Bundle b = new Bundle();
                b.putString("rideId", rideId);
                intent.putExtras(b);
                itemView.getContext().startActivity(intent);
            } else if (status.equals("On Progress")) {
                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("View Ride Details")
                        .setContentText("View ride details or cancel this ride service")
                        .setConfirmText("VIEW RIDE").setCancelText("DECLINE RIDE")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(itemView.getContext(), HistoryReserveDriverPage.class);
                                Bundle b = new Bundle();
                                b.putString("rideId", rideId);
                                intent.putExtras(b);
                                itemView.getContext().startActivity(intent);
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                openCancelReserve();
                            }
                        })
                        .show();


            }

        } else if (differenceDate <= 2) {
            if (status.equals("Done")) {
                Intent intent = new Intent(itemView.getContext(), HistoryReserveDriverPage.class);
                Bundle b = new Bundle();
                b.putString("rideId", rideId);
                intent.putExtras(b);
                itemView.getContext().startActivity(intent);
            } else if (status.equals("On Progress")) {
                final SweetAlertDialog loadingbar = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Progress Done?")
                        .setContentText("Set the progress of this ride service to DONE or view the service")
                        .setConfirmText("CONFIRM").setCancelText("VIEW RIDE")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                                loadingbar.setTitleText("Updating");
                                loadingbar.setCancelable(false);
                                loadingbar.show();

                                setHistoryTrue(rideId);
                                setDriversTrue(rideId);
                                setRiderTrue(rideId);
                                setCarTrue(rideId);
                                sweetAlertDialog.dismiss();

                                final Handler handler = new Handler();
                                final Runnable run = new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingbar.dismiss();
                                        ReserveHistoryDriver.refreshData();
                                    }
                                };
                                handler.postDelayed(run, 2000);
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Intent intent = new Intent(itemView.getContext(), HistoryReserveDriverPage.class);
                                Bundle b = new Bundle();
                                b.putString("rideId", rideId);
                                intent.putExtras(b);
                                itemView.getContext().startActivity(intent);
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();


            }
        }


        //openCancelReserve();
    }

    private void setCarTrue(final String rideId) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        setCarTrue(rideId, d.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setCarTrue(final String rideId, final String carkey) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(carkey).child("history");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey().equals(rideId)) {
                            FirebaseDatabase.getInstance().getReference("Cars").child(carkey).child("history").child(rideId).setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void setRiderTrue(final String rideId) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.riders);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        setRiderTrue(rideId, d.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setRiderTrue(final String rideId, final String riderkey) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.riders).child(riderkey).child("history");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey().equals(rideId)) {
                            FirebaseDatabase.getInstance().getReference(Common.riders).child(riderkey).child("history").child(d.getKey()).setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setDriversTrue(final String rideId) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference("Drivers").child(d.getKey()).child("history").child(rideId).setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setHistoryTrue(final String rideId) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (d.getKey().equals(rideId)) {
                            FirebaseDatabase.getInstance().getReference("History").child(rideId).child("progress").setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void openCancelReserve() {
        String rawId = rideIdReserve.getText().toString();
        final String[] parts = rawId.split(" ");
        plate = parts[1];

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("History").child(plate);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dateSet = dataSnapshot.child("date").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        AlertDialog.Builder dialog = new AlertDialog.Builder(this.itemView.getContext());
        dialog.setTitle("CANCEL RESERVATION");
        dialog.setMessage("Enter the confirmation code given by the admin");
        LayoutInflater inflater = LayoutInflater.from(this.itemView.getContext());
        final View layout = inflater.inflate(R.layout.layout_add_cancel, null);
        dialog.setView(layout);

        final MaterialEditText confirmationKey = layout.findViewById(R.id.confirmationKey);
        selectDriver = layout.findViewById(R.id.selectDriver);
        selectDriver.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                itemPosition = position;
                itemSplit = item.toString();
            }
        });

        checkAvailable();


        dialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*if (TextUtils.isEmpty(confirmationKey.getText().toString())) {
                    Snackbar.make(itemView, "Please enter confirmation code", Snackbar.LENGTH_LONG).show();
                    return;
                }*/

                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("ConfirmationCode");
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                if (confirmationKey.getText().toString().equals(d.getKey())) {
                                    String[] newItem;
                                    String driverKey;
                                    try {
                                        newItem = itemSplit.split(" ");
                                        driverKey = newItem[3];
                                    } catch (Exception e) {
                                        newItem = driverKeyAvailable.get(0).split(" ");
                                        driverKey = newItem[3];
                                    }
                                    Log.e("asd", "" + Common.currentGetuid + ", " + plate);
                                    FirebaseDatabase.getInstance().getReference("ConfirmationCode").removeValue();

                                    FirebaseDatabase.getInstance().getReference("Drivers").child(Common.currentGetuid).child("history").child(plate).removeValue();
                                    FirebaseDatabase.getInstance().getReference("History").child(plate).child("driver").removeValue();

                                    FirebaseDatabase.getInstance().getReference("Drivers").child(driverKey).child("history").child(plate).setValue(false);
                                    FirebaseDatabase.getInstance().getReference("History").child(plate).child("driver").setValue(driverKey);

                                    ReserveHistoryDriver.refreshData();
                                    new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.WARNING_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Successfully re-assigned driver")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .show();
                                } else {
                                    Snackbar.make(itemView, "Confirmation code does not match", Snackbar.LENGTH_LONG).show();
                                }
                            }

                        } else {
                            Snackbar.make(itemView, "Confirmation code does not exist", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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

    private void checkAvailable() {

        driverKeyNotAvail = new ArrayList<>();
        driverKeyAvailable = new ArrayList<>();
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (!d.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            String key = d.getKey();
                            String name = d.child("firstname").getValue().toString() + " " + d.child("lastname").getValue().toString();
                            getHistoryId(key, name);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHistoryId(final String key, final String name) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(key).child(Common.historychild);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        getDateAvail(key, d.getKey(), name);
                    }
                } else {
                    driverKeyAvailable.add(name + " - " + key);
                    selectDriver.setItems(driverKeyAvailable);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDateAvail(final String driverKey, String key, final String name) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("History").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue().equals("reserve")) {
                        if (dateSet.equals(dataSnapshot.child("date").getValue())) {
                            int a;
                            driverKeyNotAvail.add(name + " - " + driverKey);
                            for (a = 0; a < driverKeyNotAvail.size(); a++) {
                                for (int b = 0; b < driverKeyAvailable.size(); b++) {
                                    if (driverKeyNotAvail.get(a).equals(driverKeyAvailable.get(b))) {
                                        driverKeyAvailable.remove(b);
                                    }
                                }
                            }
                        } else {
                            int a;
                            driverKeyAvailable.add(name + " - " + driverKey);
                            for (a = 0; a < driverKeyNotAvail.size(); a++) {
                                for (int b = 0; b < driverKeyAvailable.size(); b++) {
                                    if (driverKeyNotAvail.get(a).equals(driverKeyAvailable.get(b))) {
                                        driverKeyAvailable.remove(b);
                                    }
                                }
                            }
                        }

                        //CHECKER
                        Set<String> hs = new HashSet<>();
                        hs.addAll(driverKeyAvailable);
                        driverKeyAvailable.clear();
                        driverKeyAvailable.addAll(hs);

                        selectDriver.setItems(driverKeyAvailable);
                    }

                } else {
                    driverKeyAvailable.add(driverKey);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
