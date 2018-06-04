package com.parse.starter.varescon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.Drivers;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Remote.IGoogleAPI;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.vlonjatg.progressactivity.ProgressRelativeLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReserveRide extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Button btnSetDate, btnSetTime, btnReserveConfirm;
    private TextView reserveFrom, reserveTo, noCarAvailText, durationReserve, reserveDistance, reservationFee, noDateAvail, dateOnlyOnce, totalAmt;
    private ImageView checkAvailImage, checkDateAvailImage;
    private ProgressBar progressCar, progressDateCar;
    Calendar now;
    private boolean setDate = false, setTime = false;
    private String reservationPickup, reservationDestinationOne, reservationDestinationTwo;
    private int tapStatus;
    private IGoogleAPI mService;
    private FirebaseDatabase db;
    private DatabaseReference drivers;
    private DatabaseReference users;
    private DatabaseReference reserve_services;
    private String[] driverKeys = new String[100];
    private int i = 0, j = 0;
    private String dateSet;
    private String timeSet;
    private MaterialSpinner btnSetTypeCar;
    private boolean setType;
    private boolean isDateAvail;
    private boolean isCarAvail;
    private ArrayList<String> carKeyNotAvail;
    private ArrayList<String> driverKeyNotAvail;
    private DatabaseReference carRef;
    private ArrayList<String> carKeyAvailable;
    private ArrayList<String> driverKeyAvailable;

    private ArrayList<String> dateExist;

    private String carItem = null;
    private boolean carAvailDataChange;
    private boolean availDataDataChange;
    private boolean reserveStatus;
    private double currentPosLong;
    private double currentPosLat;
    private double firstDesLat;
    private double firstDesLong;
    private double secondDesLat;
    private double secondDesLong;
    private DatabaseReference reserve;
    private DatabaseReference carsRef;
    private boolean driverSpottedNotAvail;
    private boolean carSpottedNotAvail;
    private double distanceMap;
    private String durationMap;
    private Double amtMap;
    Double ridePrice;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_ride);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        if (getIntent() != null) {
            reservationPickup = getIntent().getStringExtra("reservationPickup");
            reservationDestinationOne = getIntent().getStringExtra("reservationDestinationOne");
            tapStatus = getIntent().getIntExtra("tapStatus", 0);


            currentPosLat = getIntent().getDoubleExtra("currentPosLat", 0.0);
            currentPosLong = getIntent().getDoubleExtra("currentPosLong", 0.0);
            firstDesLat = getIntent().getDoubleExtra("firstDesLat", 0.0);
            firstDesLong = getIntent().getDoubleExtra("firstDesLong", 0.0);

            if (tapStatus == 2) {
                reservationDestinationTwo = getIntent().getStringExtra("reservationDestinationTwo");
                secondDesLat = getIntent().getDoubleExtra("secondDesLat", 0.0);
                secondDesLong = getIntent().getDoubleExtra("secondDesLong", 0.0);
            }
        }


        now = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                ReserveRide.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                ReserveRide.this,
                24,
                60,
                false
        );

        //INITIALIZATION
        checkDateAvailImage = findViewById(R.id.checkDateAvailImage);
        noDateAvail = findViewById(R.id.noDateAvail);
        progressDateCar = findViewById(R.id.progressDateCar);
        totalAmt = findViewById(R.id.totalAmt);
        dateOnlyOnce = findViewById(R.id.dateOnlyOnce);
        reservationFee = findViewById(R.id.reservationFee);
        reserveDistance = findViewById(R.id.reserveDistance);
        durationReserve = findViewById(R.id.durationReserve);
        btnSetTypeCar = findViewById(R.id.btnSetTypeCar);
        btnSetTypeCar.setItems(" ", "Sedan - 4 passenger seats", "Van - 8 passenger seats");
        mService = Common.getGoogleService();
        btnSetDate = findViewById(R.id.btnSetDate);
        btnSetTime = findViewById(R.id.btnSetTime);
        progressCar = findViewById(R.id.progressCar);
        reserveFrom = findViewById(R.id.reserveFrom);
        reserveTo = findViewById(R.id.reserveTo);
        btnReserveConfirm = findViewById(R.id.btnReserveConfirm);
        noCarAvailText = findViewById(R.id.noAvail);
        checkAvailImage = findViewById(R.id.checkAvailImage);

        btnReserveConfirm.setEnabled(false);
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, 3);
        datePickerDialog.setMinDate(yesterday);

        timePickerDialog.setMinTime(8, 0, 0);
        timePickerDialog.setMaxTime(19, 0, 0);


        //BUTTONS
        btnSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show(getFragmentManager(), "Date Picker Dialog");
            }
        });

        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show(getFragmentManager(), "Time Picker Dialog");
            }
        });
        btnReserveConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSet != null && carItem != null && timeSet != null && btnSetTypeCar.getSelectedIndex() != 0 && dateExist.size() < 1) {
                    new SweetAlertDialog(ReserveRide.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Reservation")
                            .setContentText("Confirm your reservation?")
                            .setConfirmText("CONFIRM").setCancelText("CANCEL")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    payPalPayment();
                                    //assignDriverAndCar();
                                }
                            })
                            .setCancelClickListener(null)
                            .show();
                } else {
                    new SweetAlertDialog(ReserveRide.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Error")
                            .setContentText("Please complete all fields without an error.")
                            .setConfirmText("CONFIRM")
                            .setConfirmClickListener(null)
                            .show();
                }
            }
        });

        btnSetTypeCar.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                setType = true;
                carItem = String.valueOf(item);
                resetLoadCar();
                if (btnSetTypeCar.getSelectedIndex() != 0) {
                    if (setDate) {
                        if (setType == false)
                            carItem = " ";
                        checkCarAvailable();
                    }
                } else {
                    resetLoadCar();
                }
            }
        });


        //SETTERS
        reserveFrom.setText(Common.startAdd);
        reserveTo.setText(Common.endAdd);
        if (tapStatus == 2)
            reserveTo.setText(Common.endAdd + "\n" + Common.endAddTwo);
        DoubleBounce db = new DoubleBounce();
        DoubleBounce db2 = new DoubleBounce();
        progressCar.setIndeterminateDrawable(db);
        progressDateCar.setIndeterminateDrawable(db2);

        noCarAvailText.setVisibility(View.GONE);
        noDateAvail.setVisibility(View.GONE);

        checkAvailImage.setVisibility(View.GONE);
        checkDateAvailImage.setVisibility(View.GONE);

        progressCar.setVisibility(View.GONE);
        progressDateCar.setVisibility(View.GONE);
        dateOnlyOnce.setVisibility(View.GONE);

        if (tapStatus == 1) {
            String[] splitted = Common.distanceText.split("\\s+");
            double distance = Double.parseDouble(splitted[0]);
            distanceMap = distance;
            double newFare = (distance * Common.reserveFee) + distance;
            String duration = String.valueOf(Common.reserveTimeValue) + " m";
            durationMap = duration;
            durationReserve.setText(duration);
            reserveDistance.setText(String.valueOf(distance) + " km");
            reservationFee.setText("Final distance fare X 25% = " + String.format("%.2f", newFare));
            Double totalamt = Common.getPrice(distance, Common.reserveTimeValue);
            amtMap = totalamt + (totalamt*Common.reserveFee);
            totalAmt.setText(String.format("P %.2f", Float.parseFloat(String.valueOf(amtMap))));
            ridePrice = totalamt + newFare;
        } else {
            String[] splitted = Common.distanceText.split("\\s+");
            String[] splittedTwo = Common.distanceTextTwo.split("\\s+");

            double distance = Double.parseDouble(splitted[0]);
            double distanceTwo = Double.parseDouble(splittedTwo[0]);

            distanceMap = (Double.parseDouble(splitted[0]) + Double.parseDouble(splittedTwo[0]));

            int durationOne = Integer.parseInt(String.valueOf(Common.reserveTimeValue));
            int durationTwo = Integer.parseInt(String.valueOf(Common.reserveTimeValueTwo));
            int finalDuration = durationOne + durationTwo;
            durationMap = finalDuration + " m";
            String duration = String.valueOf(finalDuration) + " m";

            double feeOne = Double.parseDouble(splitted[0]);
            double feeTwo = Double.parseDouble(splittedTwo[0]);
            double finalFee = feeOne + feeTwo;
            durationReserve.setText(duration);
            reserveDistance.setText(String.valueOf(distance) + " km");
            reservationFee.setText("Final distance fare X 25% = " + String.format("%.2f", finalFee));
            Double totalamt = Common.getPrice(distance, durationOne);
            Double totalamtTwo = Common.getPrice(distanceTwo, durationTwo);
            Float finalAmt = Float.parseFloat(String.valueOf(totalamt + totalamtTwo));
            amtMap = (totalamt + totalamtTwo) + ((totalamt + totalamtTwo)*Common.reserveFee);
            totalAmt.setText(String.format("P %.2f", amtMap));
            ridePrice = Double.valueOf(amtMap);
        }

    }

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PayPalConfig.PAYPAL_CLIENT_ID);
    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amtMap ), "PHP", "Varescon Ride Reservation",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm != null){
                    try{
                        JSONObject jsonObject = new JSONObject(confirm.toJSONObject().toString());
                        String paymentResponse = jsonObject.getJSONObject("response").getString("state");
                        if(paymentResponse.equals("approved")){
                            assignDriverAndCar();
                            new SweetAlertDialog(ReserveRide.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("Successfully reserve ride service")
                                    .setConfirmText("CONFIRM")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            finish();
                                            sweetAlertDialog.dismiss();
                                        }
                                    })
                                    .show();

                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void assignDriverAndCar() {
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        users = db.getReference(Common.riders);
        carsRef = db.getReference("Cars");
        reserve = db.getReference(Common.history);

        for (int a = 0; a < driverKeyAvailable.size(); a++) {
            Log.e("asd", "" + driverKeyAvailable.get(a));
        }

        Random r = new Random();
        int ranIntDriver;
        int ranIntCar;
        try {
            ranIntDriver = r.nextInt(driverKeyAvailable.size()) + 0;
        } catch (Exception e){
            ranIntDriver = 0;
        }
        try {
            ranIntCar = r.nextInt(carKeyAvailable.size()) + 0;
        } catch (Exception e){
            ranIntCar = 0;
        }

        String driverKey = driverKeyAvailable.get(ranIntDriver);
        String carKey = carKeyAvailable.get(ranIntCar);

        Log.e("driver&car", "" + driverKey + "," + carKey);

        String requestId = reserve.push().getKey();
        Common.requestId = requestId;

        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(requestId).setValue(false);

        drivers.child(driverKey).child(Common.historychild).child(requestId).setValue(false);
        carsRef.child(carKey).child(Common.historychild).child(requestId).setValue(false);


        HashMap map = new HashMap();
        map.put("car", carKey);
        map.put("date", dateSet);
        map.put("pick_up_location", Common.startAdd);
        map.put("destination", Common.endAdd);
        map.put("currentPosLat", currentPosLat);
        map.put("currentPosLong", currentPosLong);
        map.put("firstDesLat", firstDesLat);
        map.put("firstDesLong", firstDesLong);

        map.put("driver", driverKey);
        map.put("distance", distanceMap + " km");
        map.put("duration", durationMap);
        if (tapStatus == 2) {
            map.put("destination_second", Common.endAddTwo);
            map.put("secondDesLat", secondDesLat);
            map.put("secondDesLong", secondDesLong);
        }

        map.put("progress", "false");
        map.put("rider", FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("time", timeSet);
        map.put("type", "reserve");
        map.put("mode_of_payment", "PayPal");
        map.put("amt_paid", amtMap);
        Double driverSalary = (amtMap * .20);
        map.put("driver_ride_salary", driverSalary);
        reserve.child(requestId).updateChildren(map);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String monthSet = setupMonth(monthOfYear);
        btnSetDate.setBackgroundResource(R.drawable.signin_button_design);
        btnSetDate.setText("" + monthSet + " " + dayOfMonth + ", " + year);
        dateSet = "" + monthSet + " " + dayOfMonth + ", " + year;
        setDate = true;

        resetLoadDate();
        resetLoadCar();

        btnSetTypeCar.setSelectedIndex(0);
        if (setDate) {
            if (setType == false)
                carItem = " ";
            checkRiderSameDate();
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String typeOfDay = typeOfDay(hourOfDay);
        int hour = hourOfDay(hourOfDay);
        String mMinute = getMinute(minute);
        btnSetTime.setBackgroundResource(R.drawable.signin_button_design);
        btnSetTime.setText("" + hour + ":" + mMinute + " " + typeOfDay);
        timeSet = "" + hour + ":" + mMinute + " " + typeOfDay;
        setTime = true;
    }

    private void resetLoadCar() {
        noCarAvailText.setVisibility(View.GONE);
        checkAvailImage.setVisibility(View.GONE);
        progressCar.setVisibility(View.GONE);
        dateOnlyOnce.setVisibility(View.GONE);
    }

    private void resetLoadDate() {
        noDateAvail.setVisibility(View.GONE);
        checkDateAvailImage.setVisibility(View.GONE);
        progressDateCar.setVisibility(View.GONE);
        dateOnlyOnce.setVisibility(View.GONE);
    }


    private void checkRiderSameDate() {
        dateOnlyOnce.setVisibility(View.GONE);
        resetLoadDate();
        dateExist = new ArrayList<>();

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.riders).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        checkEachDate(FirebaseAuth.getInstance().getCurrentUser().getUid(), d.getKey());
                    }
                } else {
                    Log.e("bbb", "" + dateExist.size());
                    checkAvailable();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkEachDate(final String key, String historyKey) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(historyKey);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dateSet.equals(dataSnapshot.child("date").getValue()) && key.equals(dataSnapshot.child("rider").getValue()) && dataSnapshot.child("type").getValue().equals("reserve") && dataSnapshot.child("progress").getValue().equals("false")) {
                        dateExist.add(dateSet);
                        Log.e("ccc", "" + dateExist.size());
                        resetLoadDate();
                        dateOnlyOnce.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        Log.e("ddd", "" + dateExist.size());
                        if (dateExist.size() < 1)
                            checkAvailable();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void checkAvailable() {
        progressDateCar.setVisibility(View.VISIBLE);

        driverKeyNotAvail = new ArrayList<>();
        driverKeyAvailable = new ArrayList<>();
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        getHistoryId(d.getKey());
                    }
                } else {
                    if(dateExist.size() > 0){
                        resetLoadDate();
                        dateOnlyOnce.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        if (driverKeyAvailable.size() > 0) {
                            noDateAvail.setVisibility(View.GONE);
                            checkDateAvailImage.setVisibility(View.VISIBLE);
                            progressDateCar.setVisibility(View.GONE);
                            dateOnlyOnce.setVisibility(View.GONE);
                        } else {
                            noDateAvail.setVisibility(View.VISIBLE);
                            checkDateAvailImage.setVisibility(View.GONE);
                            progressDateCar.setVisibility(View.GONE);
                            dateOnlyOnce.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHistoryId(final String key) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(key).child(Common.historychild);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        getDateAvail(key, d.getKey());
                    }
                } else {
                    if(dateExist.size() > 0){
                        resetLoadDate();
                        dateOnlyOnce.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        Log.e("key available","called");
                        driverKeyAvailable.add(key);
                        if (driverKeyAvailable.size() > 0) {
                            noDateAvail.setVisibility(View.GONE);
                            checkDateAvailImage.setVisibility(View.VISIBLE);
                            progressDateCar.setVisibility(View.GONE);
                            dateOnlyOnce.setVisibility(View.GONE);
                        } else {
                            noDateAvail.setVisibility(View.VISIBLE);
                            checkDateAvailImage.setVisibility(View.GONE);
                            progressDateCar.setVisibility(View.GONE);
                            dateOnlyOnce.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDateAvail(final String driverKey, String key) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("History").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dateExist.size() > 0) {
                        resetLoadDate();
                        dateOnlyOnce.setVisibility(View.VISIBLE);
                        return;
                    } else if (dateExist.size() <= 0) {
                        if (dataSnapshot.child("type").getValue().equals("reserve")) {
                            if (dateSet.equals(dataSnapshot.child("date").getValue())) {
                                int a;
                                driverKeyNotAvail.add(String.valueOf(dataSnapshot.child("driver").getValue()));
                                for (a = 0; a < driverKeyNotAvail.size(); a++) {
                                    for (int b = 0; b < driverKeyAvailable.size(); b++) {
                                        if (driverKeyNotAvail.get(a).equals(driverKeyAvailable.get(b))) {
                                            driverKeyAvailable.remove(b);
                                        }
                                    }
                                }
                            } else {
                                int a;
                                driverKeyAvailable.add(String.valueOf(dataSnapshot.child("driver").getValue()));
                                for (a = 0; a < driverKeyNotAvail.size(); a++) {
                                    for (int b = 0; b < driverKeyAvailable.size(); b++) {
                                        if (driverKeyNotAvail.get(a).equals(driverKeyAvailable.get(b))) {
                                            driverKeyAvailable.remove(b);
                                        }
                                    }
                                }
                            }


                            Log.e("ggg", "" + dateExist.size());

                            //CHECKER

                            Set<String> hs = new HashSet<>();
                            hs.addAll(driverKeyAvailable);
                            driverKeyAvailable.clear();
                            driverKeyAvailable.addAll(hs);
                            for (int a = 0; a < driverKeyAvailable.size(); a++) {
                                Log.e("driverAvails", "" + driverKeyAvailable.get(a));
                            }
                            if (driverKeyAvailable.size() > 0) {
                                resetLoadDate();
                                checkDateAvailImage.setVisibility(View.VISIBLE);
                            } else {
                                resetLoadDate();
                                noDateAvail.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                } else {
                    if (dateExist.size() > 0) {
                        resetLoadDate();
                        dateOnlyOnce.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        driverKeyAvailable.add(driverKey);
                        if (driverKeyAvailable.size() > 0) {
                            noDateAvail.setVisibility(View.GONE);
                            checkDateAvailImage.setVisibility(View.VISIBLE);
                            progressDateCar.setVisibility(View.GONE);
                            dateOnlyOnce.setVisibility(View.GONE);
                        }
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void checkCarAvailable() {
        progressCar.setVisibility(View.VISIBLE);
        if (dateExist.size() > 0) {
            dateOnlyOnce.setVisibility(View.VISIBLE);
        }

        carKeyNotAvail = new ArrayList<>();
        carKeyAvailable = new ArrayList<>();
        carSpottedNotAvail = false;
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        if (carItem.equals(d.child("type").getValue()) && d.child("status").getValue().equals(true))
                            getReserveCarKey(d.getKey());
                        else {
                            noCarAvailText.setVisibility(View.VISIBLE);
                            checkAvailImage.setVisibility(View.GONE);
                            progressCar.setVisibility(View.GONE);
                            btnReserveConfirm.setEnabled(false);
                            btnReserveConfirm.setBackgroundResource(R.drawable.gray_out_design);
                        }
                    }
                } else {
                    noCarAvailText.setVisibility(View.VISIBLE);
                    checkAvailImage.setVisibility(View.GONE);
                    progressCar.setVisibility(View.GONE);
                    btnReserveConfirm.setEnabled(false);
                    btnReserveConfirm.setBackgroundResource(R.drawable.gray_out_design);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getReserveCarKey(final String key) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars").child(key).child("history");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        getCarAvailDate(key, d.getKey());
                    }
                } else {
                    carKeyAvailable.add(key);
                    if (carKeyAvailable.size() > 0) {
                        noCarAvailText.setVisibility(View.GONE);
                        checkAvailImage.setVisibility(View.VISIBLE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(true);
                        btnReserveConfirm.setBackgroundResource(R.drawable.signin_button_design);
                    } else {
                        noCarAvailText.setVisibility(View.VISIBLE);
                        checkAvailImage.setVisibility(View.GONE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(false);
                        btnReserveConfirm.setBackgroundResource(R.drawable.gray_out_design);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCarAvailDate(final String carKey, final String reserveKey) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("History").child(reserveKey);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue().equals("reserve")) {

                        if (dateSet.equals(dataSnapshot.child("date").getValue())) {
                            int a;
                            carKeyNotAvail.add(String.valueOf(dataSnapshot.child("car").getValue()));
                            for (a = 0; a < carKeyNotAvail.size(); a++) {
                                for (int b = 0; b < carKeyAvailable.size(); b++) {
                                    if (carKeyNotAvail.get(a).equals(carKeyAvailable.get(b))) {
                                        carKeyAvailable.remove(b--);
                                    }
                                }
                            }
                        } else {
                            int a;
                            carKeyAvailable.add(String.valueOf(dataSnapshot.child("car").getValue()));
                            for (a = 0; a < carKeyNotAvail.size(); a++) {
                                for (int b = 0; b < carKeyAvailable.size(); b++) {
                                    if (carKeyNotAvail.get(a).equals(carKeyAvailable.get(b))) {
                                        carKeyAvailable.remove(b--);
                                    }
                                }
                            }

                        }
                    }

                    //CHECK
                    Set<String> hs = new HashSet<>();
                    hs.addAll(carKeyAvailable);
                    carKeyAvailable.clear();
                    carKeyAvailable.addAll(hs);
                    for (int a = 0; a < carKeyAvailable.size(); a++) {
                        Log.e("driverAvails", "" + carKeyAvailable.get(a));
                    }

                    Log.e("Carkey", "" + carKeyAvailable.size());
                    if (carKeyAvailable.size() > 0) {
                        noCarAvailText.setVisibility(View.GONE);
                        checkAvailImage.setVisibility(View.VISIBLE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(true);
                        btnReserveConfirm.setBackgroundResource(R.drawable.signin_button_design);
                    } else {
                        noCarAvailText.setVisibility(View.VISIBLE);
                        checkAvailImage.setVisibility(View.GONE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(false);
                        btnReserveConfirm.setBackgroundResource(R.drawable.gray_out_design);
                    }
                } else {
                    carKeyAvailable.add(carKey);
                    if (carKeyAvailable.size() > 0) {
                        noCarAvailText.setVisibility(View.GONE);
                        checkAvailImage.setVisibility(View.VISIBLE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(true);
                        btnReserveConfirm.setBackgroundResource(R.drawable.signin_button_design);
                    } else {
                        noCarAvailText.setVisibility(View.VISIBLE);
                        checkAvailImage.setVisibility(View.GONE);
                        progressCar.setVisibility(View.GONE);
                        btnReserveConfirm.setEnabled(false);
                        btnReserveConfirm.setBackgroundResource(R.drawable.gray_out_design);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private String getMinute(int minute) {
        if (minute < 10)
            return "0" + minute;
        else
            return "" + minute;
    }

    private int hourOfDay(int hourOfDay) {
        if (hourOfDay <= 12)
            return hourOfDay;
        else if (hourOfDay == 13)
            return 1;
        else if (hourOfDay == 14)
            return 2;
        else if (hourOfDay == 15)
            return 3;
        else if (hourOfDay == 16)
            return 4;
        else if (hourOfDay == 17)
            return 5;
        else if (hourOfDay == 18)
            return 6;
        else if (hourOfDay == 19)
            return 7;
        else if (hourOfDay == 20)
            return 8;
        else if (hourOfDay == 21)
            return 9;
        else if (hourOfDay == 22)
            return 10;
        else if (hourOfDay == 23)
            return 11;

        return 0;
    }

    private String typeOfDay(int hourOfDay) {
        if (hourOfDay > 12)
            return "PM";
        else
            return "AM";
    }

    private String setupMonth(int monthOfYear) {
        switch (monthOfYear) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";

            default:
                return "UKN";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, PayPalService.class));
    }
}
