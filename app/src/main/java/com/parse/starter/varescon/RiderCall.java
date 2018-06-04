package com.parse.starter.varescon;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.FCMResponse;
import com.parse.starter.varescon.Model.Notification;
import com.parse.starter.varescon.Model.Sender;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Model.Uid;
import com.parse.starter.varescon.Remote.IFCMService;
import com.parse.starter.varescon.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiderCall extends AppCompatActivity {

    Boolean buttonPressed = false;

    TextView time_text, distance_text, address_text;
    Button btnAccept, btnDecline;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    RingProgressBar ringProgressBar;
    int progress = 0;

    FirebaseDatabase db;
    DatabaseReference drivers, users;

    String userUid = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (progress < 150) {
                    progress++;
                    ringProgressBar.setProgress(progress);
                }
            }
        }
    };

    String customerId, title;
    double lat, lng;

    DatabaseReference onlineRef, currentUserRef;
    MaterialAnimatedSwitch location_switch;
    private DatabaseReference history;
    private String pickupAddress;
    private String destinationAddress;
    private String requestIdLocal;
    private String custUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();


        if (getIntent() != null) {
            title = getIntent().getStringExtra("title");
            if (title.equals("Rider")) {
                setContentView(R.layout.activity_rider_call);
                lat = getIntent().getDoubleExtra("lat", -1.0);
                lng = getIntent().getDoubleExtra("lng", -1.0);
                customerId = getIntent().getStringExtra("customer");

                getDirection(lat, lng);
                getAddress(Common.pickupLat, Common.pickupLong, Common.desLat, Common.desLong);
                classInit();
            } else if (title.equals("Called")) {
                customerId = getIntent().getStringExtra("customer");
                Token token = new Token(customerId);

                com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification(title, "Driver has been hailed by another passenger");
                Sender sender = new Sender(token.getToken(), notification);

                mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
            }
        }


    }

    private void classInit() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sms);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        location_switch = findViewById(R.id.location_switch);


        //INITIALIZE VIEW
        time_text = findViewById(R.id.time_text);
        distance_text = findViewById(R.id.distance_text);
        address_text = findViewById(R.id.address_text);

        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);


        //PROGRESS
        ringProgressBar = findViewById(R.id.progressBarAD);
        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                if (!buttonPressed) {
                    cancelBooking(customerId, 1);
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 150; i++) {
                    try {
                        Thread.sleep(150);
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPressed = true;


                acceptBooking(customerId);
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPressed = true;
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId, 0);
            }
        });
    }

    private void acceptBooking(String customerId) {
        Token token = new Token(customerId);

        com.parse.starter.varescon.Model.Notification notification = new com.parse.starter.varescon.Model.Notification("Accept", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1) {
                    Toast.makeText(RiderCall.this, "Accepted", Toast.LENGTH_SHORT).show();

                    updateDriver();
                    saveRide();
                    VaresconDriverActivity.vda.finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void updateDriver() {
        db = FirebaseDatabase.getInstance();
        drivers = db.getReference(Common.drivers);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.hailed).setValue(true);
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
        users = db.getReference(Common.riders);
        history = db.getReference(Common.history);

        requestIdLocal = history.push().getKey();
        Common.requestId = requestIdLocal;
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Common.historychild).child(requestIdLocal).setValue(false);
        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("LastRequestId").setValue(requestIdLocal);
        users.child(Common.userKey).child(Common.historychild).child(requestIdLocal).setValue(false);
        users.child(Common.userKey).child("status").setValue("false");

        HashMap map = new HashMap();
        map.put("driver", FirebaseAuth.getInstance().getCurrentUser().getUid());
        map.put("rider", Common.userKey);
        map.put("pickup_location", pickupAddress);
        map.put("destination", destinationAddress);
        map.put("type", "regular");
        map.put("date", formattedDate);
        map.put("time", strDate);
        map.put("fromLat", Common.pickupLat);
        map.put("fromLong", Common.pickupLong);
        map.put("toLat", Common.desLat);
        map.put("toLong", Common.desLong);

        history.child(requestIdLocal).updateChildren(map);

        Intent intent = new Intent(RiderCall.this, DriverTracking.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("customerId", Common.userKey);
        intent.putExtra("called", true);
        intent.putExtra("requestId", requestIdLocal);

        startActivity(intent);
        finish();
    }

    private void cancelBooking(String customerId, int flag) {
        if (flag == 0) {
            Token token = new Token(customerId);

            String title = "Cancel", body = "Driver has is not available. Please try again";
            Notification data = new Notification(title, body);
            Sender content = new Sender(token.getToken(), data);

            mFCMService.sendMessage(content)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(RiderCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());
                        }
                    });

            Common.driverRequest = 0;
        } else if (flag == 1) {


            Token token = new Token(customerId);

            String title = "Late", body = "Driver did not respond in time. Please try again";
            Notification data = new Notification(title, body);
            Sender content = new Sender(token.getToken(), data);

            mFCMService.sendMessage(content)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(RiderCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());
                        }
                    });

            Common.driverRequest = 0;
        }
    }


    private void getDirection(double lat, double lng) {

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("RIDERCALL", requestApi);
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
                        distance_text.setText(distance.getString("text"));
                        //TIME
                        JSONObject time = legsObject.getJSONObject("duration");
                        time_text.setText(time.getString("text"));
                        //ADDRESS
                        String address = legsObject.getString("end_address");
                        address_text.setText(address);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RiderCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAddress(double pickupLat, double pickupLong, double desLat, double desLong) {
        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference_driving&" +
                    "origin=" + pickupLat + "," + pickupLong + "&" +
                    "destination=" + desLat + "," + desLong + "&" +
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


                        pickupAddress = legsObject.getString("start_address");
                        destinationAddress = legsObject.getString("end_address");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RiderCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            if (mediaPlayer != null)
                mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
