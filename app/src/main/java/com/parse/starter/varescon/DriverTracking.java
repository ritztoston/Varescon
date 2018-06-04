package com.parse.starter.varescon;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.parse.starter.varescon.Helper.DirectionJSONParser;
import com.parse.starter.varescon.Model.FCMResponse;
import com.parse.starter.varescon.Model.Sender;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Remote.IFCMService;
import com.parse.starter.varescon.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Driver;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.parse.starter.varescon.Common.Common.desLat;
import static com.parse.starter.varescon.Common.Common.desLong;
import static com.parse.starter.varescon.Common.Common.mLastLocation;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final int PLAY_SERVICE_RES_CODE = 7001;

    public static Activity dt;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private int getDirectionDone = 0;
    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    String customerId;

    private boolean pickedupCustomer;


    private GoogleMap mMap;
    double riderLat, riderLng;

    private Circle riderMarker, markerDestination;
    private Marker driverMarker;

    private Polyline direction;

    //POLYLINES
    private List<LatLng> polyLineList;
    private LatLng currentPosition;
    private PolylineOptions polylineOptions;
    public static Polyline greyPolyLine;
    private IGoogleAPI mPolyService;

    private boolean destinationReached = false;

    Button btnDropOffHere, cancelWalkin;

    Location pickupLocation;
    private FirebaseDatabase db;
    private DatabaseReference drivers;
    private DatabaseReference users;
    private DatabaseReference history;
    private String end_address;
    private String final_calculate;
    private boolean pickupReached = false;
    private Location destinationLocation;
    private boolean called;
    private boolean secondNotif = false;
    private String requestId;
    private DatabaseReference notavail;
    private boolean lostCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dt = this;
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapTracking);
        mapFragment.getMapAsync(this);

        if (getIntent() != null) {
            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customerId");
            called = getIntent().getBooleanExtra("called", false);
            requestId = getIntent().getStringExtra("requestId");
            try {
                lostCon = getIntent().getBooleanExtra("lostCon", false);
            } catch (Exception e) {
            }
        }
        Log.e("asd", ""+called);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        mPolyService = Common.getGoogleAPI();

        cancelWalkin = findViewById(R.id.cancelWalkin);
        btnDropOffHere = findViewById(R.id.btnDropOffHere);
        cancelWalkin.setVisibility(View.GONE);

        cancelWalkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Cancel Walk-in")
                        .setContentText("Are you sure to cancel walk-in services?")
                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                db = FirebaseDatabase.getInstance();
                                drivers = db.getReference("NotAvailable");
                                removeWalkin();
                                startActivity(new Intent(DriverTracking.this, VaresconDriverActivity.class));
                                finish();
                                sweetAlertDialog.dismiss();
                                return;
                            }
                        })
                        .show();
            }
        });

        btnDropOffHere.setEnabled(false);

        btnDropOffHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Drop Off")
                        .setContentText("Are you sure to drop off the passenger here?")
                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (pickupLocation != null) {
                                    if (called)
                                        calculateCashFee(pickupLocation, Common.mLastLocation);
                                    else if (!called) {
                                        Log.e("request", "" + riderLat);
                                        walkinCalculate(riderLat, riderLng, Common.mLastLocation);
                                    }
                                } else if (!called) {
                                    Log.e("request", "" + riderLat);
                                    walkinCalculate(riderLat, riderLng, Common.mLastLocation);
                                }
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            }
        });


        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.availDrivers);
        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

        setUpLocation();

        if (called) {
            if (lostCon) {
                Common.requestId = requestId;
                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.riders).child(customerId);
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("status").getValue().toString().equals("false")) {
                                getDestination(riderLat, riderLng);
                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(requestId);
                                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Common.desLat = Double.parseDouble(dataSnapshot.child("toLat").getValue().toString());
                                            Common.desLong = Double.parseDouble(dataSnapshot.child("toLong").getValue().toString());
                                            getDestination(riderLat, riderLng);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(requestId);
                                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Common.desLat = Double.parseDouble(dataSnapshot.child("toLat").getValue().toString());
                                            Common.desLong = Double.parseDouble(dataSnapshot.child("toLong").getValue().toString());
                                            getDestination(Double.parseDouble(dataSnapshot.child("toLat").getValue().toString()), Double.parseDouble(dataSnapshot.child("toLong").getValue().toString()));
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
            }
        } else if (!called) {
            Log.e("asd", "called");
            cancelWalkin.setVisibility(View.VISIBLE);
            btnDropOffHere.setEnabled(true);
            btnDropOffHere.setBackgroundResource(R.drawable.signin_button_design);
        }

    }


    private void calculateCashFee(final Location pickupLocation, final Location mLastLocation) {

        final String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + pickupLocation.getLatitude() + ", " + pickupLocation.getLongitude() + "&" +
                    "destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);


                        //DISTANCE
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                        //DURATION
                        JSONObject timeObject = legsObject.getJSONObject("duration");
                        String time_text = timeObject.getString("text");

                        int time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                        end_address = legsObject.getString("end_address");

                        final_calculate = String.format("P %.2f", Common.getPrice(distance_value, time_value));

                        Intent intent = new Intent(DriverTracking.this, TripDetailsPayout.class);
                        intent.putExtra("start_address", legsObject.getString("start_address"));
                        intent.putExtra("end_address", legsObject.getString("end_address"));
                        intent.putExtra("time", String.valueOf(time_value));
                        intent.putExtra("distance", String.valueOf(distance_value));
                        intent.putExtra("final_calculate", final_calculate);
                        intent.putExtra("location_start", String.format("%f,%f", pickupLocation.getLatitude(), pickupLocation.getLongitude()));
                        intent.putExtra("location_end", String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        intent.putExtra("flag", "driver");
                        intent.putExtra("requestId", requestId);
                        intent.putExtra("customerId", customerId);
                        intent.putExtra("called", called);

                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void walkinCalculate(final Double riderLat, final Double riderLng, final Location mLastLocation) {

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + riderLat + "," + riderLng + "&" +
                    "destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.e("request", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);


                        //DISTANCE
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                        //DURATION
                        JSONObject timeObject = legsObject.getJSONObject("duration");
                        String time_text = timeObject.getString("text");

                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                        end_address = legsObject.getString("end_address");

                        final_calculate = String.format("P %.2f", Common.getPrice(distance_value, time_value));

                        Intent intent = new Intent(DriverTracking.this, TripDetailsPayout.class);
                        intent.putExtra("start_address", legsObject.getString("start_address"));
                        intent.putExtra("end_address", legsObject.getString("end_address"));
                        intent.putExtra("time", String.valueOf(time_value));
                        intent.putExtra("distance", String.valueOf(distance_value));
                        intent.putExtra("final_calculate", final_calculate);
                        intent.putExtra("location_start", String.format("%f,%f", riderLat, riderLng));
                        intent.putExtra("location_end", String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        intent.putExtra("flag", "driver");
                        intent.putExtra("flag", "driver");
                        intent.putExtra("called", called);

                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_CODE).show();
            } else {
                Toast.makeText(this, "This Device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void setUpLocation() {

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
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
        if (called) {
            if (!lostCon)
                riderMarker = mMap.addCircle(new CircleOptions().center(new LatLng(riderLat, riderLng)).radius(50).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(5.0f));

            geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("NotAvailable"));
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderLat, riderLng), 0.05f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!pickupReached) {
                        pickupLocation = mLastLocation;
                        btnDropOffHere.setEnabled(true);
                        pickupReached = true;
                        btnDropOffHere.setBackgroundResource(R.drawable.signin_button_design);
                        sendArrivedNotification(Common.riderToken);

                        final Handler handler = new Handler();
                        final Runnable run = new Runnable() {
                            @Override
                            public void run() {
                                if (!pickedupCustomer) {
                                    if (!isFinishing()) {
                                        secondNotif = true;
                                        sendArrivedNotification(Common.riderToken);
                                    }
                                }
                            }
                        };
                        handler.postDelayed(run, 60000);
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });


            geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("NotAvailable"));
            GeoQuery geoQueryDest = geoFire.queryAtLocation(new GeoLocation(desLat, desLong), 0.05f);
            geoQueryDest.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!destinationReached) {
                        destinationLocation = Common.mLastLocation;
                        if (markerDestination != null)
                            markerDestination.remove();
                        destinationReached = true;
                        sendArrivedDestination(Common.riderToken);
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    }

    private void sendArrivedDestination(String customerIdd) {
        Token token = new Token(customerId);
        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Arrived Destination", "You have arrived at your destination");
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                final SweetAlertDialog arrDes = new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.SUCCESS_TYPE);
                arrDes.setTitleText("Arrived Destination")
                        .setContentText("Drop off here?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Drop Off")
                                        .setContentText("Are you sure to drop off the passenger here?")
                                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                if (pickupLocation != null)
                                                    calculateCashFee(pickupLocation, Common.mLastLocation);
                                                destinationReached = true;
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .show();

                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        arrDes.dismiss();
                    }
                };
                handler.postDelayed(runnable, 25000);
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendArrivedNotification(final String customerIdd) {
        Token token = new Token(customerIdd);
        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Arrived", String.format("The driver %s has arrived at your location", Common.currentUser.getFirstname()));
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (!isFinishing()) {
                    if (!secondNotif) {
                        new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Arrived Destination")
                                .setContentText("Have you picked up the passenger?")
                                .setConfirmText("Confirm").setCancelText("Not Yet")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        pickedupCustomer = true;
                                        if (riderMarker != null)
                                            riderMarker.remove();

                                        db = FirebaseDatabase.getInstance();
                                        users = db.getReference(Common.riders);
                                        users.child(customerId).child("status").setValue("true");

                                        getDestination(Common.desLat, Common.desLong);
                                        getDirectionDone = 1;
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
                    } else if (secondNotif) {
                        new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Notification Sent")
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        new SweetAlertDialog(DriverTracking.this, SweetAlertDialog.WARNING_TYPE)
                                                .setTitleText("Ride Confirmation")
                                                .setContentText("Have you picked up the passenger?")
                                                .setConfirmText("Confirm").setCancelText("Cancel Ride")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        pickedupCustomer = true;
                                                        if (riderMarker != null)
                                                            riderMarker.remove();
                                                        getDestination(Common.desLat, Common.desLong);
                                                        getDirectionDone = 1;
                                                        sweetAlertDialog.dismiss();

                                                        db = FirebaseDatabase.getInstance();
                                                        users = db.getReference(Common.riders);
                                                        users.child(customerId).child("status").setValue("true");
                                                    }
                                                })
                                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        if (riderMarker != null)
                                                            riderMarker.remove();
                                                        sweetAlertDialog.dismiss();
                                                        removeRide();
                                                        Token token = new Token(customerIdd);
                                                        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Cancel", "The driver has cancelled your trip due to inactivity");
                                                        Sender sender = new Sender(token.getToken(), notification);
                                                        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                                            @Override
                                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                                            }

                                                            @Override
                                                            public void onFailure(Call<FCMResponse> call, Throwable t) {

                                                            }
                                                        });
                                                        Intent intent = new Intent(DriverTracking.this, VaresconDriverActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .show();
                                        sweetAlertDialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeRide() {
        Common.driverRequest = 0;
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        users = db.getReference(Common.riders);
        history = db.getReference(Common.history);
        notavail = db.getReference("NotAvailable");

        notavail.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("hailed").setValue(false);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("LastRequestId").removeValue();
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(requestId).removeValue();
        users.child(customerId).child(Common.historychild).child(requestId).removeValue();
        history.child(requestId).removeValue();
    }

    private void removeWalkin() {
        Common.driverRequest = 0;
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        users = db.getReference(Common.riders);
        history = db.getReference(Common.history);
        notavail = db.getReference("NotAvailable");

        notavail.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("hailed").setValue(false);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(Common.requestId).removeValue();
        history.child(Common.requestId).removeValue();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();


            //UPDATE TO FIREBASE
            users = FirebaseDatabase.getInstance().getReference("NotAvailable");
            geoFire = new GeoFire(users);
            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    //ADD MAKER TO MAP
                    if (driverMarker != null)
                        driverMarker.remove();
                    driverMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("You")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxiindi)));


                    //MOVE CAMERA
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.5f));
                }
            });
            if (called) {
                    if (direction != null)
                        direction.remove();

                    if (getDirectionDone == 0)
                        getDirection();
            } else {
                Log.d("ERROR", "Cannot get your location");
            }
        }

    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + currentPosition.latitude + ", " + currentPosition.longitude + "&" +
                    "destination=" + riderLat + "," + riderLng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("VARESCON", requestApi);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        new ParserTask().execute(response.body().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (greyPolyLine != null)
            greyPolyLine.remove();
        if (!destinationReached) {
            Common.mLastLocation = location;
            displayLocation();
            if (pickedupCustomer)
                if (markerDestination != null) {
                    getDestination(Common.desLat, Common.desLong);
                }
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please wait...");
            mDialog.show();

        }


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.parseColor("#011627"));
                polylineOptions.geodesic(true);
            }
            if(direction !=null)
                direction.remove();
            direction = mMap.addPolyline(polylineOptions);
        }
    }

    private void getDestination(double desLat, double desLong) {
        Log.e("asd", "" + desLat + ", " + desLong);

        currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=walking&" +
                    "avoid=ferries&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + desLat + "," + desLong + "&" +
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
                        polylineOptions.width(10);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyLine = mMap.addPolyline(polylineOptions);

                        if (markerDestination != null)
                            markerDestination.remove();
                        markerDestination = mMap.addCircle(new CircleOptions().center(polyLineList.get(polyLineList.size() - 1)).radius(45).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(5.0f));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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


}
