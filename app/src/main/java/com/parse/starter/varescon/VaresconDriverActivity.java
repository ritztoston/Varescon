package com.parse.starter.varescon;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.DriverReportsRecycler.DriversReportInformation;
import com.parse.starter.varescon.History.HistoryHolder;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.Remote.IGoogleAPI;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.ghyeok.stickyswitch.widget.StickySwitch;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.parse.starter.varescon.Common.Common.mLastLocation;

public class VaresconDriverActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    public static Activity vda;

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_CODE = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;

    DatabaseReference users;
    GeoFire geoFire;

    Marker mCurrent;

    public StickySwitch location_switch;
    SupportMapFragment mapFragment;


    private IGoogleAPI mService;


    //PRESENCE
    DatabaseReference onlineRef, currentUserRef;

    RelativeLayout rootLayout;
    private ValueEventListener mListener;
    private FirebaseDatabase db;
    private boolean loggedOut;


    CircleImageView header_profilePicDriver;
    TextView nameDriver;
    MaterialRatingBar ratingDriver;
    private String directionFinal;
    private android.support.v4.app.Fragment fragment;
    private RelativeLayout mapHolder;
    private int fragStat = 1;
    Button walkinBtn;
    private DatabaseReference drivers;
    private DatabaseReference history;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.currentGetuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.drivers);
        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.removeEventListener(this);
                Common.currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        setContentView(R.layout.activity_varescon_driver);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        vda = this;
        rootLayout = findViewById(R.id.rootLayout);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        walkinBtn = findViewById(R.id.walkinBtn);

        //PRESENCE
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.availDrivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mListener = onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //INITIALIZE VIEW
        mapHolder = findViewById(R.id.mapHolderDriver);
        location_switch = findViewById(R.id.location_switch);
        directionFinal = "LEFT";
        location_switch.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(final StickySwitch.Direction direction, String s) {
                Log.e("", direction.name());
                if (direction.name().equals("RIGHT")) {
                    FirebaseDatabase.getInstance().goOnline();
                    DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers);
                    userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(d.getKey())) {

                                        if (d.child("assigned").getValue().toString().equals("false")) {
                                            location_switch.setDirection(StickySwitch.Direction.LEFT);
                                            directionFinal = "LEFT";
                                            new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Error")
                                                    .setContentText("You have not been assigned to a car service. Please contact the admin to be assigned")
                                                    .setConfirmText("OK")
                                                    .setConfirmClickListener(null)
                                                    .show();
                                        } else if (d.child("assigned").getValue().toString().equals("true")) {
                                            startLocationUpdates();
                                            displayLocation();
                                            directionFinal = direction.name();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                } else if (direction.name().equals("LEFT")) {
                    stopLocationUpdates();
                    FirebaseDatabase.getInstance().goOffline();
                    if (mCurrent != null)
                        mCurrent.remove();
                    if (mMap != null)
                        mMap.clear();
                    animateCamera();
                    directionFinal = direction.name();
                }
            }
        });


        //GEO FIRE
        users = FirebaseDatabase.getInstance().getReference(Common.availDrivers);
        geoFire = new GeoFire(users);

        setUpLocation();

        mService = Common.getGoogleAPI();

        updateFirebaseToken();

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

            header_profilePicDriver = header.findViewById(R.id.header_profilePicDriver);
            nameDriver = header.findViewById(R.id.nameDriver);
            ratingDriver = header.findViewById(R.id.ratingDriver);
            ratingDriver.setEnabled(false);

            FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            nameDriver.setText("" + dataSnapshot.child("firstname").getValue() + " " + dataSnapshot.child("lastname").getValue());
                            ratingDriver.setRating(Float.parseFloat(String.valueOf(dataSnapshot.child("rating").getValue())));
                            //SET PROFILE PICTURE
                            Glide
                                    .with(VaresconDriverActivity.this)
                                    .load(dataSnapshot.child("profilePic").getValue())
                                    .dontAnimate()
                                    .into(header_profilePicDriver);
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        walkinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("assigned").getValue().toString().equals("false")) {
                                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText("You have not been assigned to a car service. Please contact the admin to be assigned")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(null)
                                        .show();
                            } else if (dataSnapshot.child("assigned").getValue().toString().equals("true")) {
                                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText("Are you sure?")
                                        .setContentText("Enable OTS services?")
                                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                location_switch.setDirection(StickySwitch.Direction.RIGHT);
                                                directionFinal = "RIGHT";


                                                updateDriver();
                                                saveRide();
                                                FirebaseDatabase.getInstance().getReference(Common.availDrivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                                displayLocation();

                                                Intent intent = new Intent(VaresconDriverActivity.this, DriverTracking.class);
                                                intent.putExtra("lat", mLastLocation.getLatitude());
                                                intent.putExtra("lng", mLastLocation.getLongitude());
                                                intent.putExtra("called", false);

                                                startActivity(intent);
                                                finish();
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
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


        checkRideOnProgress();
    }

    private void checkRideOnProgress() {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        String isHailed = dataSnapshot.child("hailed").getValue().toString();
                        final String reqId = dataSnapshot.child("LastRequestId").getValue().toString();
                        if (isHailed.equals("true")) {
                            displayLocation();
                            final Handler handler = new Handler();
                            final Runnable run = new Runnable() {
                                @Override
                                public void run() {
                                    DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(reqId);
                                    userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Double lat = Double.parseDouble(dataSnapshot.child("fromLat").getValue().toString());
                                                Double lng = Double.parseDouble(dataSnapshot.child("fromLong").getValue().toString());
                                                Intent intent = new Intent(VaresconDriverActivity.this, DriverTracking.class);
                                                intent.putExtra("lat", lat);
                                                intent.putExtra("lng", lng);
                                                intent.putExtra("customerId", dataSnapshot.child("rider").getValue().toString());
                                                intent.putExtra("called", true);
                                                intent.putExtra("requestId", reqId);
                                                intent.putExtra("lostCon", true);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            };
                            handler.postDelayed(run, 8000);
                        }
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void saveRide() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = df.format(c.getTime());
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm a");
        String strDate = "" + mdformat.format(calendar.getTime());

        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        history = db.getReference(Common.history);

        String requestId = history.push().getKey();
        Common.requestId = requestId;
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(requestId).setValue(false);

        HashMap map = new HashMap();
        map.put("driver", FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("rider", "Anonymous");
        map.put("type", "regular");
        map.put("date", formattedDate);
        map.put("time", strDate);

        history.child(requestId).updateChildren(map);
    }

    private void updateDriver() {
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(true);
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference rf = db.getReference(Common.tokens);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        rf.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //REQUEST RUNTIME PERMISSION
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.getDirection().equals(StickySwitch.Direction.RIGHT)) {
                    displayLocation();
                }
            }
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

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            if (location_switch.getDirection().equals(StickySwitch.Direction.RIGHT)) {
                final double latitude = mLastLocation.getLatitude();
                final double longtitude = mLastLocation.getLongitude();

                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("hailed").getValue().toString().equals("false")) {

                                //UPDATE TO FIREBASE
                                users = FirebaseDatabase.getInstance().getReference(Common.availDrivers);
                                geoFire = new GeoFire(users);
                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //ADD MAKER TO MAP
                                        if (mCurrent != null)
                                            mCurrent.remove();
                                        mCurrent = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longtitude))
                                                .title("You")
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxiindi)));


                                        //MOVE CAMERA
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.5f));
                                    }
                                });
                            } else if (dataSnapshot.child("hailed").getValue().toString().equals("true")){
                                //UPDATE TO FIREBASE
                                users = FirebaseDatabase.getInstance().getReference("NotAvailable");
                                geoFire = new GeoFire(users);
                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //ADD MAKER TO MAP
                                        if (mCurrent != null)
                                            mCurrent.remove();
                                        mCurrent = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longtitude))
                                                .title("You")
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxiindi)));


                                        //MOVE CAMERA
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.5f));
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            } else {
                final double latitude = mLastLocation.getLatitude();
                final double longtitude = mLastLocation.getLongitude();

                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                if (dataSnapshot.child("hailed").getValue().toString().equals("true")) {
                                    //UPDATE TO FIREBASE
                                    users = FirebaseDatabase.getInstance().getReference("NotAvailable");
                                    geoFire = new GeoFire(users);
                                    geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {
                                            //ADD MAKER TO MAP
                                            if (mCurrent != null)
                                                mCurrent.remove();
                                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(latitude, longtitude))
                                                    .title("You"));


                                            //MOVE CAMERA
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.5f));
                                        }
                                    });
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {
            Log.d("ERROR", "Cannot get your location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.getDirection().equals(StickySwitch.Direction.RIGHT)) {
                            displayLocation();
                        }
                    }
                }
        }
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
        getMenuInflater().inflate(R.menu.varescon_driver, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.driver_home) {
            if (fragStat != 1 && !directionFinal.equals("RIGHT")) {
                fragStat = 1;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Welcome Driver");
                VaresconDriverActivity.this.recreate();
                overridePendingTransition(0, 0);
            } else if (fragStat == 1) {
            } else {
                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: You are online")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.driver_tripDetails) {
            if (fragStat != 2 && !directionFinal.equals("RIGHT")) {
                FirebaseDatabase.getInstance().goOnline();
                FirebaseDatabase.getInstance().getReference(Common.availDrivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                fragStat = 2;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Trip History");
                fragment = new HistoryHolder();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.regularRideHolder, fragment);
                ft.commit();
                Bundle bundle = new Bundle();
                bundle.putString("CustorDriver", "Driver");
                fragment.setArguments(bundle);
                mapHolder.setVisibility(View.GONE);
            } else if (fragStat == 2) {
            } else {
                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: You are online")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.driver_reports) {
            if (fragStat != 3 && !directionFinal.equals("RIGHT")) {
                FirebaseDatabase.getInstance().goOnline();
                FirebaseDatabase.getInstance().getReference(Common.availDrivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                fragStat = 3;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Reports");
                fragment = new DriversReportInformation();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.regularRideHolder, fragment);
                ft.commit();


                mapHolder.setVisibility(View.GONE);
            } else if (fragStat == 3) {
            } else {
                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: You are online")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.driver_settings) {
            if (fragStat != 4 && !directionFinal.equals("RIGHT")) {
                FirebaseDatabase.getInstance().goOnline();
                FirebaseDatabase.getInstance().getReference(Common.availDrivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                fragStat = 4;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Settings");
                fragment = new DriverProfileSettings();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.regularRideHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            } else if (fragStat == 4) {
            } else {
                new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Unable")
                        .setContentText("Service not available: You are online")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        } else if (id == R.id.driver_signOut) {
            new SweetAlertDialog(VaresconDriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Are you Sure to Log Out?")
                    .setConfirmText("Confirm").setCancelText("Cancel")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            Common.resetVariables();

                            Intent intent = new Intent(VaresconDriverActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(VaresconDriverActivity.this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
                            finish();
                            loggedOut = true;
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
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
        mLastLocation = location;
        displayLocation();
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

        animateCamera();


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void animateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(9.85521609, 124.17434692), 8.5f));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loggedOut) {
            db = FirebaseDatabase.getInstance();
            users = db.getReference(Common.drivers);

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
            FirebaseDatabase.getInstance().goOffline();
            stopLocationUpdates();
            FirebaseDatabase.getInstance().goOnline();
            FirebaseAuth.getInstance().signOut();
            loggedOut = false;
        }
    }
}
