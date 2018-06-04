package com.parse.starter.varescon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.AdminHistoryHolder.AdminHolder;
import com.parse.starter.varescon.AdminReports.AdminReports;
import com.parse.starter.varescon.Admins.Admin_information;
import com.parse.starter.varescon.Cars.Car_information;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Drivers.Driver_information;
import com.parse.starter.varescon.Helper.CustomInfoWindow;
import com.parse.starter.varescon.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    RelativeLayout mapHolder;

    private boolean loggedOut;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private ValueEventListener mListener;
    private int fragStat = 0;
    private Fragment fragment;

    //POLYLINES
    private List<LatLng> polyLineList;
    private LatLng currentPosition;
    private PolylineOptions polylineOptions;
    public static Polyline greyPolyLine;
    private IGoogleAPI mPolyService;

    private Marker mUserMarker, markerDestination;
    LatLng onChangeLocation;
    private int firstClick = 0;
    private Boolean isDestinationSet = false;
    private Location mLastLocation;
    int numStat = 0;
    LatLng destination;

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
    Button btnRequestPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapHolder = findViewById(R.id.mapHolder);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Common.admin = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnRequestPickup = findViewById(R.id.btnPickUpRequest);
        btnRequestPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Destination?")
                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                Intent intent = new Intent(AdminActivity.this, ReserveRide.class);
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
        });


        mPolyService = Common.getGoogleAPI();
        polyLineList = new ArrayList<>();

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
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.adminOtp) {
            if (fragStat != 1 && fragStat != 0) {
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Enable OTP Services")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                if (fragment != null) {
                                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                                    ft.commit();
                                }

                                fragStat = 1;
                                AdminActivity.this.recreate();
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
            }
        } else if (id == R.id.adminReports) {
            if (fragStat != 2) {
                fragStat = 2;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Varescon Reports");
                fragment = new AdminReports();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.adminHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            }
        } else if (id == R.id.adminTrip) {
            if (fragStat != 3) {
                fragStat = 3;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Trip History");
                fragment = new AdminHolder();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.adminHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            }
        } else if (id == R.id.adminDrivers) {
            if (fragStat != 4) {
                fragStat = 4;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Drivers Information");
                fragment = new Driver_information();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.adminHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            }
        } else if (id == R.id.adminCars) {
            if (fragStat != 5) {
                fragStat = 5;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Cars Information");
                fragment = new Car_information();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.adminHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            }
        } else if (id == R.id.adminAdmin) {
            if (fragStat != 6) {
                fragStat = 6;
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().remove(fragment);
                    ft.commit();
                }
                getSupportActionBar().setTitle("Admin");
                fragment = new Admin_information();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.adminHolder, fragment);
                ft.commit();
                mapHolder.setVisibility(View.GONE);
            }

        } else if (id == R.id.adminSignout) {
            new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Are you Sure to Log Out?")
                    .setConfirmText("Confirm").setCancelText("Cancel")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            Common.resetVariables();

                            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(AdminActivity.this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loggedOut) {
            db = FirebaseDatabase.getInstance();
            users = db.getReference("Admins");

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
            users.child(Common.admin).removeEventListener(mListener);
            users.child(Common.admin).child("online").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            });
            FirebaseAuth.getInstance().signOut();
            loggedOut = false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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

        animateCamera();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMapClickListener(latLng);

            }

        });


        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latAni, longAni), 15.5f));
        mMap.setOnMarkerClickListener(this);
    }

    private void animateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(9.669376, 123.863457), 12.5f));
    }

    private void setMapClickListener(final LatLng latLng) {
        if (fragStat == 0 || fragStat == 1) {
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
                            new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.ERROR_TYPE)
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
                    Toast.makeText(AdminActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker != null) {
            if (marker.getTitle().equals("Destination")) {
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to remove your destination?")
                        .setConfirmText("Confirm").setCancelText("Cancel")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.ERROR_TYPE)
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
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            } else if (marker.getTitle().equals("Pick up destination")) {
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                                    new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                                    new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                                    new SweetAlertDialog(AdminActivity.this, SweetAlertDialog.WARNING_TYPE)
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
}
