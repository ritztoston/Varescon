package com.parse.starter.varescon.Helper;

import android.content.Intent;
import android.media.Rating;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.RiderActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingComments extends AppCompatActivity {

    private int count;
    private double totalStars;

    private MaterialEditText comment;
    private MaterialRatingBar ratingBar;
    private String requestId;
    private FirebaseDatabase db;
    private DatabaseReference history;
    private double ratingFinal;
    private DatabaseReference driver;
    private String driverId;

    private Button btnNotnow, btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_comments);

        ratingBar = findViewById(R.id.ratingBar);
        comment = findViewById(R.id.comment);
        btnNotnow = findViewById(R.id.btnNotnow);
        btnSend = findViewById(R.id.btnSend);

        if(getIntent() != null){
            requestId = getIntent().getStringExtra("requestId");
            driverId = getIntent().getStringExtra("driverId");
        }

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingFinal = rating;
            }
        });

        comment.setMaxCharacters(20);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(comment.getText().toString())) {
                    Snackbar.make(getCurrentFocus(), "Please provide a comment!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (comment.getText().toString().length() > 20) {
                    Snackbar.make(getCurrentFocus(), "Exceeded the limit of 20 characters!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                rateDriver();
                Intent intent = new Intent(RatingComments.this, RiderActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnNotnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RatingComments.this, RiderActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void rateDriver() {
        String commentFinal;
        count = 0;
        totalStars = 0.0;
        if(TextUtils.isEmpty(comment.getText().toString()))
            commentFinal = "No comment";
        else
            commentFinal = comment.getText().toString();

        db = FirebaseDatabase.getInstance();

        history = db.getReference(Common.history);
        HashMap map = new HashMap();
        map.put("rating", String.valueOf(ratingFinal));
        map.put("comment", commentFinal);
        history.child(requestId).updateChildren(map);

        driver = db.getReference(Common.drivers);
        driver.child(driverId).child("all_rating").child(requestId).setValue(String.valueOf(ratingBar.getRating()));


        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(driverId).child("all_rating");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        checkAllHistKeys(d.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkAllHistKeys(String key) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(driverId).child("all_rating").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                        try {
                            totalStars += Double.parseDouble(dataSnapshot.getValue().toString());
                            count++;
                            driver.child(driverId).child("rating").setValue(String.valueOf(totalStars / count));
                        }catch (Exception e) {
                            driver.child(driverId).child("rating").setValue(String.valueOf(ratingBar.getRating()));
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
