package com.parse.starter.varescon;

import android.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Helper.RatingComments;
import com.parse.starter.varescon.Model.FCMResponse;
import com.parse.starter.varescon.Model.Sender;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Remote.IFCMService;
import com.parse.starter.varescon.Remote.IGoogleAPI;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.parse.starter.varescon.Common.Common.mLastLocation;

public class TripDetailsPayout extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView textDate, textBasefare, textDuration, textDistance, estPayout, bigEstPayout, textFrom, textTo, header;
    private Button btnCancelPayout, btnAcceptPayout;

    //POLYLINES
    private List<LatLng> polyLineList;
    private PolylineOptions polylineOptions;
    public static Polyline greyPolyLine;
    private IGoogleAPI mPolyService;

    private Marker markerDestination;

    IFCMService mFCMService;

    private String date;

    private String flag;
    private String[] location_end;
    private String[] location_start;
    private DatabaseReference history;
    private DatabaseReference users;
    private LatLng dropOff;
    private LatLng start_address;
    private String modeOfPayment;

    String start_addWalkin;
    private String requestId;
    private String driverId;
    private DatabaseReference driver;

    private double totalStars;
    private boolean called;
    LinearLayout buttonsHolder;
    RelativeLayout walkinHolders;
    Button btnWalkinConfirm;
    private boolean btnReceivePayment = false;
    private Double ridePrice;
    private String reqId;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details_payout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapTripDetails);

        mapFragment.getMapAsync(this);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        //INITIALIZE VIEW
        textDate = findViewById(R.id.date);
        textBasefare = findViewById(R.id.baseFare);
        textDuration = findViewById(R.id.duration);
        textDistance = findViewById(R.id.distance);
        estPayout = findViewById(R.id.estimatedPayout);
        bigEstPayout = findViewById(R.id.bigPayout);
        textFrom = findViewById(R.id.fromAdd);
        textTo = findViewById(R.id.toAdd);
        header = findViewById(R.id.headerESTI);
        mFCMService = Common.getFCMService();
        buttonsHolder = findViewById(R.id.buttonsHolder);
        btnCancelPayout = findViewById(R.id.btnCancelPayout);
        btnAcceptPayout = findViewById(R.id.btnAcceptPayout);
        walkinHolders = findViewById(R.id.walkinHolders);
        btnWalkinConfirm = findViewById(R.id.btnWalkinConfirm);
        mPolyService = Common.getGoogleAPI();


        if (getIntent() != null) {
            start_addWalkin = getIntent().getStringExtra("start_address");
            flag = getIntent().getStringExtra("flag");
            called = getIntent().getBooleanExtra("called", false);
            reqId = getIntent().getStringExtra("requestId");
            customerId = getIntent().getStringExtra("customerId");
        }

        if (flag.equals("driver")) {
            if (called) {
                buttonsHolder.setVisibility(View.VISIBLE);
                btnWalkinConfirm.setVisibility(View.GONE);
            } else {
                buttonsHolder.setVisibility(View.GONE);
                btnWalkinConfirm.setVisibility(View.VISIBLE);
            }

            btnWalkinConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Payment")
                            .setContentText("Confirm customer paid?")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    modeOfPayment = "Cash";
                                    updateRide();

                                    Common.driverRequest = 0;

                                    if (DriverTracking.dt != null)
                                        DriverTracking.dt.finish();


                                    db = FirebaseDatabase.getInstance();
                                    drivers = db.getReference(Common.drivers);
                                    drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(false);
                                    drivers = db.getReference("NotAvailable");
                                    drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();


                                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Transaction Complete")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                    startActivity(new Intent(TripDetailsPayout.this, VaresconDriverActivity.class));
                                                    finish();
                                                }
                                            })
                                            .show();
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            btnAcceptPayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Token token = new Token(Common.riderToken);
                    String body = "" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "," + Common.requestId + "," + date + "," + getIntent().getStringExtra("final_calculate") + "," + String.format("P %.2f", Common.base_fare) + "," + String.format("%s min", getIntent().getStringExtra("time")) +
                            "," + String.format("%s km", getIntent().getStringExtra("distance")) + "," + getIntent().getStringExtra("location_end") +
                            "," + getIntent().getStringExtra("location_start") +
                            "," + getIntent().getStringExtra("start_address") + "," + getIntent().getStringExtra("end_address");

                    com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Mode of Payment", body);
                    Sender sender = new Sender(token.getToken(), notification);
                    mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            try {
                                if (response.body().success == 1) {
                                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Wait for customer payout")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .show();
                                    btnAcceptPayout.setEnabled(false);
                                    btnAcceptPayout.setBackgroundResource(R.drawable.gray_out_design);
                                    btnCancelPayout.setEnabled(false);
                                    btnCancelPayout.setBackgroundResource(R.drawable.gray_out_design);
                                    header.setText("WAIT FOR CUSTOMER'S RESPONSE");
                                }
                            }catch (Exception e){
                                final SweetAlertDialog paid = new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE);
                                paid.setTitleText("Mode of payment")
                                        .setContentText("Please let the customer pay in cash")
                                        .setConfirmText("CONFIRM")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                btnReceivePayment = true;
                                                paid.dismiss();
                                                new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Success")
                                                        .setContentText("Customer paid with cash and the availed service completed")
                                                        .setConfirmText("CONFIRM")
                                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                sweetAlertDialog.dismiss();

                                                                modeOfPayment = "Cash";
                                                                updateRide();

                                                                Common.driverRequest = 0;

                                                                if (DriverTracking.dt != null)
                                                                    DriverTracking.dt.finish();


                                                                db = FirebaseDatabase.getInstance();
                                                                drivers = db.getReference(Common.drivers);
                                                                drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(false);
                                                                drivers = db.getReference("NotAvailable");
                                                                drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                                                startActivity(new Intent(TripDetailsPayout.this, VaresconDriverActivity.class));
                                                                finish();
                                                                return;
                                                            }
                                                        })
                                                        .show();
                                            }
                                        })
                                        .show();
                            }

                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                        }
                    });

                    final Handler handler = new Handler();
                    final Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            if (!btnReceivePayment) {
                                if (!isFinishing()) {
                                    final SweetAlertDialog paid = new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE);
                                    paid.setTitleText("Mode of payment")
                                            .setContentText("Customer paid in cash. Confirm this action")
                                            .setConfirmText("CONFIRM")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    btnReceivePayment = true;
                                                    paid.dismiss();
                                                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                                            .setTitleText("Success")
                                                            .setContentText("Customer paid with cash and the availed service completed")
                                                            .setConfirmText("CONFIRM")
                                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                @Override
                                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                    sweetAlertDialog.dismiss();
                                                                    Token token = new Token(Common.riderToken);

                                                                    com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Transaction Complete", "Transaction Complete");
                                                                    Sender sender = new Sender(token.getToken(), notification);
                                                                    mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                                                        @Override
                                                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                                            if (response.body().success == 1) {

                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                                                                        }
                                                                    });

                                                                    modeOfPayment = "Cash";
                                                                    updateRide();

                                                                    Common.driverRequest = 0;

                                                                    if (DriverTracking.dt != null)
                                                                        DriverTracking.dt.finish();


                                                                    db = FirebaseDatabase.getInstance();
                                                                    drivers = db.getReference(Common.drivers);
                                                                    drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(false);
                                                                    drivers = db.getReference("NotAvailable");
                                                                    drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                                                    startActivity(new Intent(TripDetailsPayout.this, VaresconDriverActivity.class));
                                                                    finish();
                                                                    return;
                                                                }
                                                            })
                                                            .show();
                                                }
                                            })
                                            .show();
                                }
                            }
                        }
                    };
                    handler.postDelayed(run, 90000);

                }
            });

            btnCancelPayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else if (flag.equals("rider")) {
            btnAcceptPayout.setText("Cash");
            btnCancelPayout.setText("PayPal");
            btnCancelPayout.setBackgroundResource(R.drawable.signin_button_design);

            btnAcceptPayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Are you sure to pay with cash?")
                            .setConfirmText("OK").setCancelText("CANCEL")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    Token token = new Token(Common.driverToken);
                                    modeOfPayment = "Cash";
                                    com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Paid", "The customer paid with cash");
                                    Sender sender = new Sender(token.getToken(), notification);

                                    mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {

                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                                        }
                                    });
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                }
            });
            btnCancelPayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Are you sure to pay through PayPal?")
                            .setConfirmText("OK").setCancelText("CANCEL")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    payPalPayment();
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style_json));
            if (!isSuccess)
                Log.e("ERROR", "Map style load failed.");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }
        mMap = googleMap;

        if (flag.equals("driver"))
            settingInformation();
        else if (flag.equals("rider"))
            settingRiderInformation();
    }

    private void settingRiderInformation() {
        if (getIntent() != null) {
            driverId = getIntent().getStringExtra("driverId");
            requestId = getIntent().getStringExtra("requestId");
            textDate.setText(getIntent().getStringExtra("date"));
            bigEstPayout.setText(getIntent().getStringExtra("payout"));
            estPayout.setText(getIntent().getStringExtra("payout"));
            textBasefare.setText(String.format("P %.2f", Common.base_fare));
            textDuration.setText(String.format("%s", getIntent().getStringExtra("time")));
            textDistance.setText(String.format("%s", getIntent().getStringExtra("distance")));
            textFrom.setText(getIntent().getStringExtra("from"));
            textTo.setText(getIntent().getStringExtra("to"));
            String[] split = getIntent().getStringExtra("payout").split(" ");
            ridePrice = Double.valueOf(split[1]);
            String[] location_end = getIntent().getStringExtra("location_end").split(",");
            LatLng dropOff = new LatLng(Double.parseDouble(location_end[0]), Double.parseDouble(location_end[1]));

            String[] location_start = getIntent().getStringExtra("location_start").split(",");
            LatLng start_address = new LatLng(Double.parseDouble(location_start[0]), Double.parseDouble(location_start[1]));

            mMap.addMarker(new MarkerOptions().position(start_address)
                    .title("Picked Up Here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            getDirection(start_address, dropOff);
        }
    }

    private void settingInformation() {
        if (getIntent() != null) {
            Calendar calendar = Calendar.getInstance();
            date = String.format("%s %d, %d", convertToDayOfWeek(calendar.get(calendar.DAY_OF_WEEK)), calendar.get(calendar.DAY_OF_MONTH), calendar.get(calendar.YEAR));
            textDate.setText(date);
            bigEstPayout.setText(getIntent().getStringExtra("final_calculate"));
            estPayout.setText(getIntent().getStringExtra("final_calculate"));
            textBasefare.setText(String.format("P %.2f", Common.base_fare));
            textDuration.setText(String.format("%s min", getIntent().getStringExtra("time")));
            textDistance.setText(String.format("%s km", getIntent().getStringExtra("distance")));
            textFrom.setText(getIntent().getStringExtra("start_address"));
            textTo.setText(getIntent().getStringExtra("end_address"));

            location_end = getIntent().getStringExtra("location_end").split(",");
            dropOff = new LatLng(Double.parseDouble(location_end[0]), Double.parseDouble(location_end[1]));

            location_start = getIntent().getStringExtra("location_start").split(",");
            start_address = new LatLng(Double.parseDouble(location_start[0]), Double.parseDouble(location_start[1]));

            mMap.addMarker(new MarkerOptions().position(start_address)
                    .title("Picked Up Here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            getDirection(start_address, dropOff);
        }
    }

    private void getDirection(LatLng start_address, LatLng dropOff) {
        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=walking&" +
                    "avoid=ferries&" +
                    "origin=" + start_address.latitude + "," + start_address.longitude + "&" +
                    "destination=" + dropOff.latitude + "," + dropOff.longitude + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            mPolyService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);
                        }
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : polyLineList)
                            builder.include(latLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                        mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#011627"));
                        polylineOptions.width(15);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyLine = mMap.addPolyline(polylineOptions);

                        if (markerDestination != null)
                            markerDestination.remove();
                        markerDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                .position(polyLineList.get(polyLineList.size() - 1)).title("Dropped Off Here"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(TripDetailsPayout.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private String convertToDayOfWeek(int day) {
        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";

            default:
                return "UKN";
        }
    }

    private String messageFromFCM;
    private String messageTitle;
    private FirebaseDatabase db;
    private DatabaseReference drivers;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (flag.equals("driver")) {
                messageTitle = intent.getExtras().getString("Title");
                messageFromFCM = intent.getExtras().getString("Message");

                if (messageTitle.equals("Paid")) {
                    if (!btnReceivePayment) {
                        final SweetAlertDialog paid = new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE);
                        paid.setTitleText("Mode of payment")
                                .setContentText("" + messageFromFCM + ". Confirm this action")
                                .setConfirmText("CONFIRM")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        btnReceivePayment = true;
                                        paid.dismiss();
                                        new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Success")
                                                .setContentText("Customer paid with cash and the availed service completed")
                                                .setConfirmText("CONFIRM")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        sweetAlertDialog.dismiss();
                                                        Token token = new Token(Common.riderToken);

                                                        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Transaction Complete", "Transaction Complete");
                                                        Sender sender = new Sender(token.getToken(), notification);
                                                        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                                            @Override
                                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                                if (response.body().success == 1) {

                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<FCMResponse> call, Throwable t) {

                                                            }
                                                        });

                                                        modeOfPayment = "Cash";
                                                        updateRide();

                                                        Common.driverRequest = 0;

                                                        if (DriverTracking.dt != null)
                                                            DriverTracking.dt.finish();


                                                        db = FirebaseDatabase.getInstance();
                                                        drivers = db.getReference(Common.drivers);
                                                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(false);
                                                        drivers = db.getReference("NotAvailable");
                                                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                                        startActivity(new Intent(TripDetailsPayout.this, VaresconDriverActivity.class));
                                                        finish();
                                                        return;
                                                    }
                                                })
                                                .show();
                                    }
                                })
                                .show();
                    }
                }
                else if (messageTitle.equals("PaidPayPal")) {
                    if (!btnReceivePayment) {
                        new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Customer paid through PayPal account")
                                .setConfirmText("CONFIRM")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        Token token = new Token(Common.riderToken);

                                        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Transaction Complete", "Transaction Complete");
                                        Sender sender = new Sender(token.getToken(), notification);
                                        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                if (response.body().success == 1) {

                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<FCMResponse> call, Throwable t) {

                                            }
                                        });

                                        modeOfPayment = "PayPal";
                                        updateRide();

                                        Common.driverRequest = 0;

                                        if (DriverTracking.dt != null)
                                            DriverTracking.dt.finish();


                                        db = FirebaseDatabase.getInstance();
                                        drivers = db.getReference(Common.drivers);
                                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(false);
                                        drivers = db.getReference("NotAvailable");
                                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

                                        startActivity(new Intent(TripDetailsPayout.this, VaresconDriverActivity.class));
                                        finish();
                                        return;
                                    }
                                })
                                .show();

                    }
                }
            } else if (flag.equals("rider")) {
                messageTitle = intent.getExtras().getString("Title");
                messageFromFCM = intent.getExtras().getString("Message");

                if (messageTitle.equals("Transaction Complete")) {

                    Intent ratingComments = new Intent(TripDetailsPayout.this, RatingComments.class);

                    ratingComments.putExtra("driverId", driverId);
                    ratingComments.putExtra("requestId", requestId);

                    startActivity(ratingComments);
                    finish();
                }
            }
        }
    };


    private void updateRide() {
        db = FirebaseDatabase.getInstance();
        history = db.getReference(Common.history);
        drivers = db.getReference(Common.drivers);
        users = db.getReference(Common.riders);

        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(Common.requestId).setValue(true);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("LastRequestId").removeValue();
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("all_rating").child(Common.requestId).setValue(0.0);

        if (called) {
            users.child(customerId).child(Common.historychild).child(Common.requestId).setValue(true);
            users.child(customerId).child("status").setValue("false");
        }
        HashMap map = new HashMap();
        map.put("duration", String.format("%s", getIntent().getStringExtra("time")));
        map.put("distance", String.format("%s km", getIntent().getStringExtra("distance")));
        map.put("destination", getIntent().getStringExtra("end_address"));
        map.put("mode_of_payment", modeOfPayment);
        map.put("fromLat", start_address.latitude);
        map.put("fromLong", start_address.longitude);
        map.put("toLat", dropOff.latitude);
        map.put("toLong", dropOff.longitude);
        map.put("amt_paid", getIntent().getStringExtra("final_calculate"));
        String[] amt = getIntent().getStringExtra("final_calculate").split(" ");
        Double amt_paid = Double.parseDouble(amt[1]);
        if (!called)
            map.put("pickup_location", getIntent().getStringExtra("start_address"));
        Double driverSalary = (amt_paid * .15);

        map.put("driver_ride_salary", driverSalary);
        map.put("amt_paid", getIntent().getStringExtra("final_calculate"));
        map.put("rating", "0");
        map.put("comment", "No Comment");

        history.child(Common.requestId).updateChildren(map);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver), new IntentFilter("MyData"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(ridePrice), "PHP", "Varescon Regular Ride",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(confirm.toJSONObject().toString());
                        String paymentResponse = jsonObject.getJSONObject("response").getString("state");
                        if (paymentResponse.equals("approved")) {
                            Token token = new Token(Common.driverToken);

                            com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("PaidPayPal", "The customer paid through PayPal");
                            Sender sender = new Sender(token.getToken(), notification);

                            mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1) {

                                    }
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {

                                }
                            });
                            new SweetAlertDialog(TripDetailsPayout.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("Successfully paid the ride service")
                                    .setConfirmText("CONFIRM")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            Intent ratingComments = new Intent(TripDetailsPayout.this, RatingComments.class);

                                            ratingComments.putExtra("driverId", driverId);
                                            ratingComments.putExtra("requestId", requestId);

                                            startActivity(ratingComments);
                                            finish();
                                            sweetAlertDialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, PayPalService.class));
    }
}
