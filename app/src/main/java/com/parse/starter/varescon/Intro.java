package com.parse.starter.varescon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.Token;
import com.parse.starter.varescon.Model.User;
import com.vlonjatg.progressactivity.ProgressRelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Intro extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3500;
    private String isUser = null;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseAuth auth;
    ProgressRelativeLayout progressRelativeLayout;

    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        progressRelativeLayout = findViewById(R.id.progressAct);
        progressRelativeLayout.showLoading();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
                int number = sharedPref.getInt("isLogged", 0);
                if (number == 0) {

                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                    prefEditor.putInt("isLogged", 1);
                    prefEditor.commit();


                    Intent homeIntent = new Intent(Intro.this, MainActivity.class);
                    startActivity(homeIntent);
                    finish();

                } else if (number == 1) {
                    isUser = sharedPref.getString("isUser", null);
                    final String email = sharedPref.getString("email", null);
                    final String pass = sharedPref.getString("pass", null);
                    try {
                        if (isUser.equals("rider")) {
                            auth = FirebaseAuth.getInstance();
                            db = FirebaseDatabase.getInstance();
                            users = db.getReference(Common.riders);

                            try {
                                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                prefEditor.putString("isUser", "rider");
                                                prefEditor.putString("email", email);
                                                prefEditor.putString("pass", pass);
                                                prefEditor.commit();
                                                Intent homeIntent = new Intent(Intro.this, RiderActivity.class);
                                                startActivity(homeIntent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                Intent homeIntent = new Intent(Intro.this, MainActivity.class);
                                startActivity(homeIntent);
                                finish();
                            }


                        } else if (isUser.equals("driver")) {
                            auth = FirebaseAuth.getInstance();
                            db = FirebaseDatabase.getInstance();
                            users = db.getReference(Common.drivers);

                            try {
                                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                prefEditor.putString("isUser", "driver");
                                                prefEditor.putString("email", email);
                                                prefEditor.putString("pass", pass);
                                                prefEditor.commit();

                                                Intent homeIntent = new Intent(Intro.this, VaresconDriverActivity.class);
                                                startActivity(homeIntent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                Intent homeIntent = new Intent(Intro.this, MainActivity.class);
                                startActivity(homeIntent);
                                finish();
                            }
                        } else if (isUser.equals("admin")) {
                            auth = FirebaseAuth.getInstance();
                            db = FirebaseDatabase.getInstance();
                            users = db.getReference("Admins");

                            try {
                                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                prefEditor.putString("isUser", "admin");
                                                prefEditor.putString("email", email);
                                                Common.getAdminEmail = email;
                                                prefEditor.putString("pass", pass);
                                                prefEditor.commit();

                                                Intent homeIntent = new Intent(Intro.this, AdminActivity.class);
                                                startActivity(homeIntent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                Intent homeIntent = new Intent(Intro.this, MainActivity.class);
                                startActivity(homeIntent);
                                finish();
                            }
                        }
                    } catch (Exception e) {
                        Intent homeIntent = new Intent(Intro.this, MainActivity.class);
                        startActivity(homeIntent);
                        finish();
                    }
                }

            }
        }, SPLASH_TIME_OUT);
    }
}
