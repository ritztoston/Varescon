package com.parse.starter.varescon;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Helper.CustomInfoWindow;
import com.parse.starter.varescon.History.HistoryHolder;
import com.parse.starter.varescon.Model.FCMResponse;
import com.parse.starter.varescon.Model.Notification;
import com.parse.starter.varescon.Model.Sender;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.Remote.IFCMService;
import com.parse.starter.varescon.Remote.IGoogleAPI;
import com.rengwuxian.materialedittext.MaterialEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.*;

public class RiderActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private ValueEventListener mListener;


    //MAP FRAGMENT
    SupportMapFragment mapFragment;
    Fragment fragment = null;

    //MAP LOCATION
    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_CODE = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    FirebaseDatabase db;
    DatabaseReference users;
    GeoFire geoFire;

    public static Marker mUserMarker, markerDestination;

    //BOTTOM SHEET
    CircleImageView headerProfilePic;
    BottomSheetRiderFragment mBottomSheet;
    Button btnRequestPickup;

    CardView requestHolder;


    boolean isDriverFound = false;
    String driverId = null;
    int radius = 1, distance = 3, LIMIT = 10, sendRequestFlag = 0;
    LatLng destination;
    int numStat = 0;


    //PRESENCE
    DatabaseReference driversAvailable;

    PlaceAutocompleteFragment place_location, place_destination;

    String mPlaceLocation, mPlaceDestination;

    AutocompleteFilter typeFilter;

    TextView header_fname, header_email;
    String userUrl, userEmail;


    //
    CoordinatorLayout regularRideHolder;
    RelativeLayout searchHolder, mapHolder, riderSettings;
    //LinearLayout riderSettings;

    //FRAGMENT STATUS
    //0 - Home
    //1 - Booking
    //2 - Trip
    //3 - Settings
    private static int fragStat = 0;

    public static int firstClick = 0;

    //RIDER STATUS
    Boolean riderStatus = false;

    public static Boolean isDestinationSet = false;
    IFCMService mService;

    SweetAlertDialog waitingDialogDriver, waitDriverResponse;
    int driverResponseFlag = 0;
    LatLng onChangeLocation;

    //POLYLINES
    private List<LatLng> polyLineList;
    private LatLng currentPosition;
    private PolylineOptions polylineOptions;
    public static Polyline greyPolyLine;
    private IGoogleAPI mPolyService;

    DatabaseReference onlineRef, currentUserRef;

    public static Activity ra;

    FirebaseAuth auth;
    private boolean loggedOut;

    //MARKER FOR RESERVATION
    private Marker reservePickup, reserveDestination, reserveDestinationTwo;
    private int tapStatus = 0;
    private LatLng currentPos, firstPos, secondPos;
    private DatabaseReference driver;
    private Marker driverMarker;
    private String reservationDestinationOne;
    private String reservationDestinationTwo;
    private String reservationPickup;
    private Polyline greyPolyLineTwo;
    private boolean markerRemoved;
    private double latAni;
    private double longAni;
    private boolean oneTimeAnimate = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.plate_no = null;

        setContentView(R.layout.activity_rider);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ra = this;

        waitingDialogDriver = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDialogDriver.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
        waitingDialogDriver.setTitleText("Finding You A Driver");
        waitingDialogDriver.setCancelable(false);

        waitDriverResponse = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        waitDriverResponse.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
        waitDriverResponse.setTitleText("Waiting for Driver's response");
        waitDriverResponse.setCancelable(false);


        mService = Common.getFCMService();

        mPolyService = Common.getGoogleAPI();
        polyLineList = new ArrayList<>();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setTitle("Home");
        //
        regularRideHolder = findViewById(R.id.contentFragment);
        searchHolder = findViewById(R.id.search_holder);
        mapHolder = findViewById(R.id.mapHolder);
        riderSettings = findViewById(R.id.rootLayout);
        searchHolder.setVisibility(View.GONE);

        requestHolder = findViewById(R.id.requestHolder);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            FirebaseDatabase.getInstance().getReference("Rates").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    try {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            FirebaseDatabase.getInstance().getReference("Rates").child(d.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot lastSnap) {
                                    try {
                                        Common.base_fare = Double.parseDouble(lastSnap.child("base_fare").getValue().toString());
                                        Common.time_fare = Double.parseDouble(lastSnap.child("time_fare").getValue().toString());
                                        Common.distance_fare = Double.parseDouble(lastSnap.child("distance_fare").getValue().toString());
                                        Log.e("asd", "" + Common.base_fare);

                                    } catch (Exception ex) {
                                        //TODO
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    } catch (Exception ex) {
                        //TODO
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            header_email = header.findViewById(R.id.header_email);
            header_fname = header.findViewById(R.id.header_fname);
            headerProfilePic = header.findViewById(R.id.header_profilePic);

            FirebaseDatabase.getInstance().getReference(Common.riders).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Common.currentUser = dataSnapshot.getValue(User.class);
                        header_fname.setText("" + Common.currentUser.getFirstname() + " " + Common.currentUser.getLastname());
                        header_email.setText(Common.currentUser.getEmail());
                        userUrl = Common.currentUser.getProfilePic().toString();
                        userEmail = Common.currentUser.getEmail();

                        //SET PROFILE PICTURE
                        Glide
                                .with(RiderActivity.this)
                                .load(userUrl)
                                .dontAnimate()
                                .into(headerProfilePic);
                    } catch (Exception ex) {
                        //TODO
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        //MAPS
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestPickup = findViewById(R.id.btnPickUpRequest);

        btnRequestPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragStat == 0) {
                    if (isDestinationSet) {
                        driverResponseFlag = 1;
                        sendRequestFlag = 0;
                        requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    } else {
                        btnRequestPickup.setText("Request Pickup");
                        new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Tap your destination")
                                .setContentText("Tap on the map to set your destination")
                                .setConfirmText("OK")
                                .setConfirmClickListener(null)
                                .show();
                    }
                } else if (fragStat == 1) {

                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Destination?")
                            .setConfirmText("CONFIRM").setCancelText("CANCEL")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    Intent intent = new Intent(RiderActivity.this, ReserveRide.class);
                                    if (tapStatus == 2) {
                                        intent.putExtra("tapStatus", 1);
                                        intent.putExtra("currentPosLat", currentPos.latitude);
                                        intent.putExtra("currentPosLong", currentPos.longitude);
                                        intent.putExtra("firstDesLat", firstPos.latitude);
                                        intent.putExtra("firstDesLong", firstPos.longitude);
                                    } else if (tapStatus == 3) {
                                        intent.putExtra("tapStatus", 2);
                                        intent.putExtra("currentPosLat", currentPos.latitude);
                                        intent.putExtra("currentPosLong", currentPos.longitude);
                                        intent.putExtra("firstDesLat", firstPos.latitude);
                                        intent.putExtra("firstDesLong", firstPos.longitude);
                                        intent.putExtra("secondDesLat", secondPos.latitude);
                                        intent.putExtra("secondDesLong", secondPos.longitude);
                                    }

                                    btnRequestPickup.setEnabled(false);
                                    btnRequestPickup.setBackgroundResource(R.drawable.gray_out_design);
                                    mMap.clear();
                                    tapStatus = 0;
                                    startActivity(intent);
                                    if (reservePickup != null)
                                        reservePickup.remove();
                                    if (reserveDestination != null)
                                        reserveDestination.remove();
                                    if (reserveDestinationTwo != null)
                                        reserveDestinationTwo.remove();
                                }
                            })
                            .setCancelClickListener(null)
                            .show();
                }
            }
        });

        //FILTER ONLY BOHOL
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .setCountry("PH")
                .setTypeFilter(3)
                .build();


        /*place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();

                mMap.clear();

                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker()).title("Pickup Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                if (markerDestination != null) {
                    markerDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).position(destination).title("Destination").snippet("Click to remove destination"));

                }
            }

            @Override
            public void onError(Status status) {

            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                //SHOW INFORMATION
                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }

            @Override
            public void onError(Status status) {

            }
        });*/

        //PRESENCE
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.pickupRequest).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        FirebaseDatabase.getInstance().goOnline();
        setUpLocation();
        updateFirebaseToken();
        Common.firstOpened = true;
    }


    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference rf = db.getReference(Common.tokens);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        rf.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void sendRequestToDriver(final String driverId) {
        final DatabaseReference rf = FirebaseDatabase.getInstance().getReference(Common.tokens);
        rf.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rf.removeEventListener(this);
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token token = postSnapShot.getValue(Token.class);
                    String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    String riderToken = FirebaseInstanceId.getInstance().getToken();
                    Notification data = new Notification(riderToken, json_lat_lng);
                    Common.driverToken = token.getToken();
                    Sender content = new Sender(token.getToken(), data);

                    mService.sendMessage(content)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1) {
                                        waitDriverResponse.show();
                                        final Handler handler = new Handler();
                                        final Runnable run = new Runnable() {
                                            @Override
                                            public void run() {
                                                if (driverResponseFlag == 1) {
                                                    waitDriverResponse.dismiss();
                                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                            .setTitleText("Error")
                                                            .setContentText("Request Timed Out")
                                                            .setConfirmText("OK")
                                                            .setConfirmClickListener(null)
                                                            .show();
                                                    driverResponseFlag = 0;
                                                    btnRequestPickup.setText("REQUEST PICKUP");
                                                    riderStatus = false;
                                                }
                                            }
                                        };
                                        handler.postDelayed(run, 30000);

                                    } else
                                        makeText(RiderActivity.this, "Failed Sent", LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rf.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rf.removeEventListener(this);
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token token = postSnapShot.getValue(Token.class);
                    String json_lat_lng = new Gson().toJson(new LatLng(destination.latitude, destination.longitude));
                    Notification data = new Notification("Destination", json_lat_lng);
                    Sender content = new Sender(token.getToken(), data);

                    mService.sendMessage(content)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1) {


                                    } else
                                        makeText(RiderActivity.this, "Failed Sent", LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rf.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rf.removeEventListener(this);
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token token = postSnapShot.getValue(Token.class);
                    String userkey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Notification data = new Notification("UserKey", userkey);
                    Sender content = new Sender(token.getToken(), data);

                    mService.sendMessage(content)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1) {


                                    } else
                                        makeText(RiderActivity.this, "Failed Sent", LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickupRequest);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        isDriverFound = false;
        if (mUserMarker.isVisible())
            mUserMarker.remove();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        btnRequestPickup.setText("Finding you a Driver...");

        radius = 1;
        findDriver();
    }

    private void findDriver() {
        waitingDialogDriver.show();
        final Handler handler = new Handler();


        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.availDrivers);
        GeoFire gfDrivers = new GeoFire(drivers);

        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {


                final String driverKey = key;
                if (!isDriverFound) {
                    sendRequestFlag = 0;
                    driverResponseFlag = 0;
                    waitingDialogDriver.dismiss();
                    isDriverFound = true;

                    if (!isFinishing()) {
                        SweetAlertDialog driverFoundDialog = new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE);
                        driverFoundDialog.setTitleText("Driver Found")
                                .setContentText("Confirming this process can't be undone and other services will be unavailable neither the destination. Are you sure to send driver a request?")
                                .setConfirmText("Confirm").setCancelText("Cancel")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        driverId = driverKey;
                                        sendRequestToDriver(driverId);
                                    }
                                })
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        btnRequestPickup.setText("REQUEST PICKUP");
                                        sweetAlertDialog.dismiss();
                                        driverId = "";
                                    }
                                })
                                .show();
                    }
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
                if (!isDriverFound && radius <= LIMIT) {
                    radius++;
                    sendRequestFlag++;
                    findDriver();
                    Log.e("radius", "" + radius);
                    if (radius > LIMIT) {
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (!isDriverFound) {
                                    if (mUserMarker.isInfoWindowShown())
                                        mUserMarker.hideInfoWindow();
                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("No Driver Found")
                                            .setContentText("No Driver is Available for the Moment")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .show();
                                }
                                btnRequestPickup.setText("REQUEST PICKUP");
                                waitingDialogDriver.dismiss();
                            }
                        };
                        handler.postDelayed(runnable, 10000);
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //REQUEST RUNTIME PERMISSION
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            /*//9.85521609,124.17434692
            //final LatLngBounds BOHOL = new LatLngBounds(new LatLng(9.588010, 123.720305), new LatLng(9.588010, 123.720305));

            *//*LatLng center = new LatLng(9.866099,124.201998);
            LatLng northSide = SphericalUtil.computeOffset(center, 1000, 0);
            LatLng southSide = SphericalUtil.computeOffset(center, 1000, 180);

            LatLngBounds BOHOL = LatLngBounds.builder()
                    .include(northSide)
                    .include(southSide)
                    .build();*//*

            //place_location.setBoundsBias(BOHOL);
            place_location.setFilter(typeFilter);

            //place_destination.setBoundsBias(BOHOL);
            place_destination.setBoundsBias(new LatLngBounds(new LatLng(9.588010, 123.720305), new LatLng(9.588010, 123.720305)));
            place_destination.setFilter(typeFilter);*/


            //PRESENCE
            if (driverId == null)
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.availDrivers);
            else if (driverId != null)
                driversAvailable = FirebaseDatabase.getInstance().getReference("NotAvailable");

            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            final double latitude = mLastLocation.getLatitude();
            final double longtitude = mLastLocation.getLongitude();
            latAni = latitude;
            longAni = longtitude;

            ///ADD MAKER TO MAP
            if (mUserMarker != null)
                mUserMarker.remove();
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longtitude))
                    .title("You"));

            //MOVE CAMERA
            if (!oneTimeAnimate) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.5f));
                oneTimeAnimate = true;
            }

            loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

            Log.d("VARESCON", String.format("Your location was changed: %f / %f", latitude, longtitude));

        } else {
            Log.d("ERROR", "Cannot get your location");
        }
    }

    private void loadAllAvailableDrivers(final LatLng location) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("You"));
        if (markerDestination != null && onChangeLocation != null)
            getDirection(destination);

        if (driverId == null) {
            DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.availDrivers);
            GeoFire gf = new GeoFire(driverLocation);

            GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
            geoQuery.removeAllListeners();

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, final GeoLocation location) {
                    FirebaseDatabase.getInstance().getReference(Common.drivers).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            //if (driverMarker != null)
                             //   driverMarker.remove();

                            //ADD DRIVER TO MAP
                            driverMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.latitude, location.longitude))
                                    .flat(true)
                                    .title("Driver: " + user.getFirstname() + " " + user.getLastname())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxiindi)));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (distance <= LIMIT) {
                        distance++;
                        loadAllAvailableDrivers(location);
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        } else if (driverId != null) {
            Log.e("asd", driverId);
            DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference("NotAvailable");
            GeoFire gf = new GeoFire(driverLocation);

            GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
            geoQuery.removeAllListeners();

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(final String key, final GeoLocation location) {
                    FirebaseDatabase.getInstance().getReference(Common.drivers).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Log.e("asd", "" + key);
                            Log.e("asd", "" + location.latitude + ", " + location.longitude);


                            //ADD DRIVER TO MAP
                            if (driverId.equals(key)) {
                                if (driverMarker != null)
                                    driverMarker.remove();
                                driverMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title("Driver: " + user.getFirstname() + " " + user.getLastname())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxiindi)));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (distance <= LIMIT) {
                        distance++;
                        loadAllAvailableDrivers(location);
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
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
                makeText(this, "This Device is not supported", LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.homeRider) {
            if (fragStat != 0 && !riderStatus) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Enable Regular Services")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                if (fragment != null) {
                                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                                    ft.commit();
                                }

                                fragStat = 0;
                                getSupportActionBar().setTitle("Home");
                                mapHolder.setVisibility(View.VISIBLE);
                                searchHolder.setVisibility(View.GONE);

                                RiderActivity.this.recreate();
                                overridePendingTransition(0, 0);


                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();

            } else if (fragStat == 0) {
            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.reserveRider) {

            if (fragStat != 1 && !riderStatus) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Enable Booking Services")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                if (fragment != null) {
                                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                                    ft.commit();
                                }
                                stopLocationUpdates();
                                getSupportActionBar().setTitle("Booking");
                                fragStat = 1;
                                mapHolder.setVisibility(View.VISIBLE);
                                searchHolder.setVisibility(View.VISIBLE);
                                reserveRider();
                                btnRequestPickup.setEnabled(false);
                                btnRequestPickup.setText("CONFIRM BOOKING");
                                btnRequestPickup.setBackgroundResource(R.drawable.gray_out_design);

                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();

            } else if (fragStat == 1) {
            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.tripHistory) {
            if (!riderStatus) {
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                fragStat = 2;
                getSupportActionBar().setTitle("Trip History");
                fragment = new HistoryHolder();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.regularRideHolder, fragment);
                ft.commit();
                Bundle bundle = new Bundle();
                bundle.putString("CustorDriver", "Customer");
                fragment.setArguments(bundle);
                mapHolder.setVisibility(View.GONE);
                searchHolder.setVisibility(View.GONE);
            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.settings) {
            if (!riderStatus) {
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                fragStat = 3;
                getSupportActionBar().setTitle("Settings");
                fragment = new RiderProfileSettings();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.regularRideHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
                searchHolder.setVisibility(View.GONE);
            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }

        } else if (id == R.id.signout) {
            if (!riderStatus) {
                fragStat = 0;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you Sure to Log Out?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                Common.resetVariables();
                                loggedOut = true;
                                Intent intent = new Intent(RiderActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(RiderActivity.this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //float zoom = mMap.getMaxZoomLevel();
        final LatLngBounds BOHOL = new LatLngBounds(new LatLng(9.588010, 123.720305), new LatLng(10.150607, 124.615822));

        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style_json));
            if (!isSuccess)
                Log.e("ERROR", "Map style load failed.");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setMinZoomPreference(11.0f);
        mMap.setMaxZoomPreference(20.0f);

        mMap.setLatLngBoundsForCameraTarget(BOHOL);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                setMapClickListener(latLng);
                /*if (LATLOCATION < NORTHEASTLAT && LONGLOCATION < SOUTHEASTLONG && LATLOCATION < SPECIALSOUTHLAT && LONGLOCATION < SPECIALSOUTHLONG)
                    setMapClickListener(latLng);*/
                /*if (LATLOCATION > NORTHEASTLAT && LONGLOCATION > NORTHEASTLONG && LATLOCATION > NORTHEASTLONG && LONGLOCATION > SOUTHEASTLONG && LATLOCATION > SOUTHEASTLAT && LONGLOCATION > SOUTHEASTLONG && LATLOCATION > SPECIALSOUTHLAT && LONGLOCATION > SPECIALSOUTHLONG)
                    setMapClickListener(latLng);*/
                /*else {
                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Out of bounds")
                            .setConfirmText("OK")
                            .setConfirmClickListener(null)
                            .show();
                }*/
            }

        });


        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latAni, longAni), 15.5f));
        mMap.setOnMarkerClickListener(this);
    }

    private void setMapClickListener(final LatLng latLng) {
        if (fragStat == 0) {
            int status = 0;
            if (riderStatus)
                status = 1;
            if (status == 0) {
                if (markerDestination != null) {
                    onChangeLocation = latLng;
                    if (firstClick == 1) {
                        new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
                                .setContentText("Are you sure to change destination?")
                                .setConfirmText("Confirm").setCancelText("Cancel")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        markerDestination.remove();
                                        greyPolyLine.remove();
                                        isDestinationSet = false;
                                        sweetAlertDialog.dismiss();

                                        markerRemoved = false;
                                        updateMap(latLng);
                                        getDirection(latLng);

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

                }

                if (firstClick == 0) {
                    onChangeLocation = latLng;
                    firstClick = 1;
                    updateMap(latLng);
                    getDirection(latLng);
                }

            } else {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: Trip on progress")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (fragStat == 1) {
            if (tapStatus < 4)
                numStat = tapStatus++;
            onClickReserve(latLng, numStat);
        }
    }

    private void onClickReserve(LatLng latLng, int i) {
        if (i == 0) {
            reservePickup = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latLng.latitude, latLng.longitude)).title("Pick up destination"));
            getReserveDirection(latLng, i);
            checkLocation(latLng);
        }
        if (i == 1) {
            btnRequestPickup.setEnabled(true);
            btnRequestPickup.setBackgroundResource(R.drawable.signin_button_design);
            getReserveDirection(latLng, i);
            updateReserveBottom(latLng, false);
        }

        if (i == 2) {
            getReserveDirection(latLng, i);
            updateReserveBottom(latLng, true);
        }
    }

    private void checkLocation(LatLng latLng) {
        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + latLng.latitude + ", " + latLng.longitude + "&" +
                    "destination=" + latLng.latitude + "," + latLng.longitude + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("VARESCON", requestApi);
            mPolyService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        String start_address = legsObject.getString("start_address");
                        String restrict_cebu = "cebu", restrict_road = "unnamed";
                        if (start_address.toLowerCase().contains(restrict_cebu.toLowerCase()) || start_address.toLowerCase().contains(restrict_road.toLowerCase())) {
                            new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Invalid route destination")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            if (greyPolyLine != null)
                                                greyPolyLine.remove();
                                            if (reservePickup != null)
                                                reservePickup.remove();
                                            tapStatus = 0;
                                            sweetAlertDialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RiderActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateReserveBottom(LatLng latLng, boolean b) {
        destination = latLng;
        BottomSheetReserve mBottomSheet;

        if (!b) {
            mBottomSheet = BottomSheetReserve.newInstance(String.format("%f,%f", currentPos.latitude, currentPos.longitude), String.format("%f,%f", latLng.latitude, latLng.longitude), true, false);
            reservationPickup = String.format("%f,%f", currentPos.latitude, currentPos.longitude);
            reservationDestinationOne = String.format("%f,%f", latLng.latitude, latLng.longitude);
        } else {
            mBottomSheet = BottomSheetReserve.newInstance(String.format("%f,%f", firstPos.latitude, firstPos.longitude), String.format("%f,%f", latLng.latitude, latLng.longitude), true, true);
            reservationDestinationTwo = String.format("%f,%f", latLng.latitude, latLng.longitude);
        }

        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    private void getReserveDirection(LatLng latLng, final int i) {
        if (i == 0)
            currentPos = new LatLng(latLng.latitude, latLng.longitude);
        else if (i == 1)
            firstPos = new LatLng(latLng.latitude, latLng.longitude);
        else if (i == 2)
            secondPos = new LatLng(latLng.latitude, latLng.longitude);

        String requestApi = null;
        try {
            if (i == 1)
                requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "mode=walking&" +
                        "avoid=ferries&" +
                        "origin=" + currentPos.latitude + "," + currentPos.longitude + "&" +
                        "destination=" + firstPos.latitude + "," + firstPos.longitude + "&" +
                        "key=" + getResources().getString(R.string.google_direction_api);
            else if (i == 2)
                requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "mode=walking&" +
                        "avoid=ferries&" +
                        "origin=" + firstPos.latitude + "," + firstPos.longitude + "&" +
                        "destination=" + secondPos.latitude + "," + secondPos.longitude + "&" +
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
                        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        //for (LatLng latLng : polyLineList)
                        //    builder.include(latLng);
                        //LatLngBounds bounds = builder.build();
                        //CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                        //mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#011627"));
                        polylineOptions.width(15);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        if (i == 1) {
                            greyPolyLine = mMap.addPolyline(polylineOptions);


                            reserveDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                    .position(polyLineList.get(polyLineList.size() - 1)).title("First Destination"));
                        } else if (i == 2) {
                            greyPolyLineTwo = mMap.addPolyline(polylineOptions);
                            reserveDestinationTwo = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .position(polyLineList.get(polyLineList.size() - 1)).title("Second Destination"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    //Toast.makeText(RiderActivity.this, "HEY: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDirection(LatLng latLng) {
        currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=walking&" +
                    "avoid=ferries&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + latLng.latitude + "," + latLng.longitude + "&" +
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
                        //LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        //for (LatLng latLng : polyLineList)
                        //    builder.include(latLng);
                        //LatLngBounds bounds = builder.build();
                        //CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                        //mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#011627"));
                        polylineOptions.width(10);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        if (greyPolyLine != null)
                            greyPolyLine.remove();
                        greyPolyLine = mMap.addPolyline(polylineOptions);

                        if (markerDestination != null)
                            markerDestination.remove();
                        markerDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                .position(polyLineList.get(polyLineList.size() - 1)).title("Destination").snippet("Click to remove destination"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RiderActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

        if (markerDestination != null && onChangeLocation != null) {
            Log.e("asd", "" + onChangeLocation);
            getDirection(onChangeLocation);
        }
    }


    private String messageFromFCM;
    private String messageTitle;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageTitle = intent.getExtras().getString("Title");
            messageFromFCM = intent.getExtras().getString("Message");
            Log.e("Driver Request 2", "Received: " + messageTitle);

            if (messageTitle.equals("Cancel")) {
                riderStatus = false;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Cancelled")
                        .setContentText("" + messageFromFCM)
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                riderStatus = false;
                                requestHolder.setVisibility(View.VISIBLE);
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                driverResponseFlag = 0;
                waitDriverResponse.dismiss();
                btnRequestPickup.setText("REQUEST PICKUP");
            } else if (messageTitle.equals("Accept")) {

                riderStatus = true;
                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("VehicleDrivers");
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            waitDriverResponse.dismiss();
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("VehicleDrivers").child(d.getKey());
                                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if (dataSnapshot.child("type").getValue().equals("TAXI - 4 passenger seats") && dataSnapshot.child("driver").getValue().equals(messageFromFCM)) {

                                                AlertDialog.Builder dialog = new AlertDialog.Builder(RiderActivity.this);
                                                dialog.setTitle("Request Accepted");


                                                LayoutInflater inflater = LayoutInflater.from(RiderActivity.this);
                                                View reauthenticate = inflater.inflate(R.layout.driver_accept, null);
                                                dialog.setView(reauthenticate);

                                                final CircleImageView profilePic  = reauthenticate.findViewById(R.id.profilePic);
                                                final TextView driverName = reauthenticate.findViewById(R.id.driverName);
                                                TextView plate_no = reauthenticate.findViewById(R.id.plate_no);


                                                FirebaseDatabase.getInstance().getReference(Common.riders).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        try {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            driverName.setText("Name: "+Common.currentUser.getFirstname()+" "+Common.currentUser.getLastname());
                                                            userUrl = Common.currentUser.getProfilePic().toString();
                                                            //SET PROFILE PICTURE
                                                            Glide
                                                                    .with(RiderActivity.this)
                                                                    .load(userUrl)
                                                                    .dontAnimate()
                                                                    .into(profilePic);
                                                        } catch (Exception ex)
                                                        {
                                                            //TODO
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                plate_no.setText("Plate Number: "+dataSnapshot.child("plate_no").getValue().toString());
                                                dialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        requestHolder.setVisibility(View.GONE);
                                                        displayLocation();
                                                        dialog.dismiss();
                                                    }
                                                });
                                                dialog.show();
                                                driverResponseFlag = 0;



                                                FirebaseDatabase.getInstance().goOffline();
                                                FirebaseDatabase.getInstance().goOnline();
                                                Common.plate_no = dataSnapshot.child("plate_no").getValue().toString();
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

            } else if (messageTitle.equals("Late")) {
                riderStatus = false;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Driver in Idle")
                        .setContentText("" + messageFromFCM)
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
                btnRequestPickup.setText("REQUEST PICKUP");
                driverResponseFlag = 0;
                waitDriverResponse.dismiss();
            } else if (messageTitle.equals("Called")) {
                riderStatus = false;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Driver Unavailable")
                        .setContentText("" + messageFromFCM)
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
                btnRequestPickup.setText("REQUEST PICKUP");
                driverResponseFlag = 0;
                waitDriverResponse.dismiss();
            } else if (messageTitle.equals("Arrived")) {
                riderStatus = false;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Driver Arrived")
                        .setContentText("" + messageFromFCM + "\n\nPlate No.: " + Common.plate_no)
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                btnRequestPickup.setText("REQUEST PICKUP");
                driverResponseFlag = 0;
                waitDriverResponse.dismiss();
            } else if (messageTitle.equals("Arrived Destination")) {
                riderStatus = false;
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Arrived Destination")
                        .setContentText("" + messageFromFCM)
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
                if (markerDestination != null)
                    markerDestination.remove();
                btnRequestPickup.setText("REQUEST PICKUP");
                driverResponseFlag = 0;
                waitDriverResponse.dismiss();
            } else if (messageTitle.equals("Mode of Payment")) {
                Log.e("LAST", "" + messageFromFCM);
                String[] message = messageFromFCM.split(",");
                String driverId = message[0], requestId = message[1], dateA = message[2], dateB = message[3], payout = message[4], baseFare = message[5],
                        time = message[6], distance = message[7],
                        location_endA = message[8], location_endB = message[9],
                        location_startA = message[10], location_startB = message[11],

                        fromA = message[12], fromB = message[13], fromC = message[14], fromD = message[15],
                        toA = message[16], toB = message[17], toC = message[18], toD = message[19], toE = null;

                Intent intentD = new Intent(RiderActivity.this, TripDetailsPayout.class);

                try {
                    if (message[20] != null)
                        toE = message[20];
                    intentD.putExtra("to", "" + toA + "," + toB + "," + toC + "," + toD + "," + toE);
                } catch (Exception e) {
                    intentD.putExtra("to", "" + toA + "," + toB + "," + toC + "," + toD);
                }


                intentD.putExtra("driverId", driverId);
                intentD.putExtra("requestId", requestId);
                intentD.putExtra("date", "" + dateA + "," + dateB);
                intentD.putExtra("payout", payout);
                intentD.putExtra("baseFare", baseFare);
                intentD.putExtra("time", time);
                intentD.putExtra("distance", distance);
                intentD.putExtra("location_end", "" + location_endA + ", " + location_endB);
                intentD.putExtra("location_start", "" + location_startA + ", " + location_startB);
                intentD.putExtra("from", "" + fromA + "," + fromB + "," + fromC + "," + fromD);

                intentD.putExtra("flag", "rider");


                startActivity(intentD);

                isDestinationSet = false;
                riderStatus = false;
                btnRequestPickup.setText("REQUEST PICKUP");
                driverResponseFlag = 0;
                firstClick = 0;

                finish();
            }
        }
    };

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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker != null) {
            if (marker.getTitle().equals("Destination")) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to remove your destination?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (!riderStatus) {
                                    firstClick = 0;
                                    marker.remove();
                                    greyPolyLine.remove();
                                    isDestinationSet = false;
                                    onChangeLocation = null;
                                    sweetAlertDialog.dismiss();
                                } else {
                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Error")
                                            .setContentText("Unable to change or remove: Trip on progress")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            } else if (marker.getTitle().equals("Pick up destination")) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to remove your pick up destination?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (tapStatus == 1) {
                                    if (greyPolyLine != null)
                                        greyPolyLine.remove();
                                    marker.remove();
                                    currentPos = null;
                                    sweetAlertDialog.dismiss();
                                    tapStatus = 0;
                                } else {
                                    sweetAlertDialog.dismiss();
                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                                            .setTitleText("Error")
                                            .setContentText("Remove first your destination")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();

            } else if (marker.getTitle().equals("First Destination")) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to remove your destination?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (tapStatus == 2) {
                                    tapStatus--;
                                    if (greyPolyLine != null)
                                        greyPolyLine.remove();
                                    marker.remove();
                                    firstPos = null;
                                    sweetAlertDialog.dismiss();
                                    btnRequestPickup.setBackgroundResource(R.drawable.gray_out_design);
                                    btnRequestPickup.setEnabled(false);
                                } else {
                                    sweetAlertDialog.dismiss();
                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                                            .setTitleText("Error")
                                            .setContentText("Remove first your destination")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            } else if (marker.getTitle().equals("Second Destination")) {
                new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to remove your destination?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (tapStatus >= 3) {
                                    tapStatus = 2;
                                    if (greyPolyLineTwo != null)
                                        greyPolyLineTwo.remove();
                                    marker.remove();
                                    secondPos = null;
                                    sweetAlertDialog.dismiss();
                                } else {
                                    sweetAlertDialog.dismiss();
                                    new SweetAlertDialog(RiderActivity.this, SweetAlertDialog.WARNING_TYPE)
                                            .setTitleText("Error")
                                            .setContentText("Remove first your destination")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
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
        }
        return true;
    }

    public void updateMap(LatLng latLng) {
        isDestinationSet = true;
        destination = latLng;


        BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude), true);
        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
    }

    private void reserveRider() {
        mMap.clear();
        tapStatus = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loggedOut) {

            db = FirebaseDatabase.getInstance();
            users = db.getReference(Common.riders);

            SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putInt("isLogged", 0);
            prefEditor.putString("isUser", null);
            prefEditor.putString("email", null);
            prefEditor.putString("pass", null);
            prefEditor.commit();


            mListener = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(mListener);
            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            });
            FirebaseAuth.getInstance().signOut();
            loggedOut = false;
        }
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}
