package com.parse.starter.varescon.History.HistoryRecycler;

import android.graphics.Color;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.Remote.IGoogleAPI;
import com.parse.starter.varescon.TripDetailsPayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorySinglePage extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView fromTd, toTd, distanceTd, durationTd, paymentTd, mopTd, driverName, comments;
    CircleImageView profile_Pic;
    private String rideId;
    private MaterialRatingBar ratingBar;

    //POLYLINES
    private List<LatLng> polyLineList;
    private PolylineOptions polylineOptions;
    public static Polyline greyPolyLine;
    private IGoogleAPI mPolyService;
    private Marker markerDestination;
    private Marker markerPickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single_page);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapTripDetails);

        mapFragment.getMapAsync(this);

        mPolyService = Common.getGoogleAPI();
        fromTd = findViewById(R.id.fromTd);
        toTd = findViewById(R.id.toTd);
        distanceTd = findViewById(R.id.distanceTd);
        durationTd = findViewById(R.id.durationTd);
        paymentTd = findViewById(R.id.paymentTd);
        mopTd = findViewById(R.id.mopTd);
        profile_Pic = findViewById(R.id.profilePicS);
        driverName = findViewById(R.id.driverName);
        ratingBar = findViewById(R.id.ratingBarSingle);
        comments = findViewById(R.id.comments);

        if (getIntent() != null) {
            String rawId = getIntent().getStringExtra("rideId");
            String[] parts = rawId.split(" ");
            rideId = parts[2];
        }

        fetchDate();
    }

    private void fetchDate() {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(rideId);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    fromTd.setText("" + dataSnapshot.child("pickup_location").getValue());
                    toTd.setText("" + dataSnapshot.child("destination").getValue());
                    distanceTd.setText("" + dataSnapshot.child("distance").getValue());
                    durationTd.setText("" + dataSnapshot.child("duration").getValue());
                    paymentTd.setText("" + dataSnapshot.child("amt_paid").getValue());
                    mopTd.setText("" + dataSnapshot.child("mode_of_payment").getValue());

                    String commentsString;
                    String ratingsString = null;
                    try{
                        if (dataSnapshot.child("comment").getValue().equals("No Comment")) {
                            commentsString = " \"" + dataSnapshot.child("comment").getValue()+"\"";
                        }
                        else{
                            commentsString = String.valueOf(dataSnapshot.child("comment").getValue());
                        }
                    }catch(Exception e){
                        commentsString = " \"" + dataSnapshot.child("comment").getValue()+"\"";
                    }
                    try{
                        Float rating = Float.parseFloat(String.valueOf(dataSnapshot.child("rating").getValue()));
                        if (rating != 0.0 || rating != 0) {
                            ratingBar.setVisibility(View.VISIBLE);
                            ratingBar.setRating(rating);
                            ratingBar.setEnabled(false);
                        }
                        else{
                            ratingBar.setVisibility(View.GONE);
                            ratingsString = "\nNo Ratings";
                        }
                    }catch (Exception e){
                        ratingBar.setVisibility(View.GONE);
                        ratingsString = "No Ratings";
                    }

                    if(commentsString != null || ratingsString != null){
                        if(commentsString != null && ratingsString != null){
                            comments.setText(ratingsString +"\nComment: "+ commentsString);
                        }
                        else if(commentsString != null && ratingsString == null){
                            comments.setText("\nComment: "+ commentsString);
                        }
                        else if(commentsString == null && ratingsString != null){
                            comments.setText("\n"+ ratingsString);
                        }
                    }




                    try {
                        getDirection(dataSnapshot.child("fromLat").getValue().toString(), dataSnapshot.child("fromLong").getValue().toString(), dataSnapshot.child("toLat").getValue().toString(), dataSnapshot.child("toLong").getValue().toString());
                        getDriverProfile(dataSnapshot.child("driver").getValue().toString());
                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverProfile(String driver) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(driver);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //SET PROFILE PICTURE
                    Glide
                            .with(HistorySinglePage.this)
                            .load(dataSnapshot.child("profilePic").getValue().toString())
                            .dontAnimate()
                            .into(profile_Pic);

                    driverName.setText("Driver's name: " + dataSnapshot.child("firstname").getValue() + " " + dataSnapshot.child("lastname").getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void getDirection(final String fromLat, final String fromLong, String toLat, String toLong) {
        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=walking&" +
                    "avoid=ferries&" +
                    "origin=" + fromLat + "," + fromLong + "&" +
                    "destination=" + toLat + "," + toLong + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.e("request", requestApi);
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
                        if (markerPickup != null)
                            markerPickup.remove();
                        markerPickup = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(fromLat), Double.parseDouble(fromLong))).title("You"));
                        markerDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                .position(polyLineList.get(polyLineList.size() - 1)).title("Dropped Off Here"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

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
