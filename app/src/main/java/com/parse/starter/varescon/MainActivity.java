package com.parse.starter.varescon;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    Button btnSign, btnReg;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.riders);


        btnSign = findViewById(R.id.btnSign);
        btnReg = findViewById(R.id.btnReg);
        rootLayout = findViewById(R.id.rootLayout);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign In");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signIn_layout = inflater.inflate(R.layout.layout_signin, null);

        final MaterialEditText editEmail = signIn_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = signIn_layout.findViewById(R.id.editPassword);


        dialog.setView(signIn_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();




                mListener = null;
                users = null;

                //CHECK VALIDATION
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Email Address", Snackbar.LENGTH_LONG).show();
                    btnSign.setEnabled(true);
                    return;
                }
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                    btnSign.setEnabled(true);
                    return;
                }

                boolean indicator = isEmailValid(editEmail.getText().toString());
                if(!indicator){
                    Snackbar.make(rootLayout, "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                }

                final SweetAlertDialog waitingDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                waitingDialog.setTitleText("Logging In");
                waitingDialog.setCancelable(false);
                waitingDialog.show();


                //LOGIN USER
                auth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();


                        users = db.getReference(Common.riders);
                        mListener = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(mListener);

                                Common.currentUser = dataSnapshot.getValue(User.class);
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                try {
                                    String status = Common.currentUser.getIdentity();
                                    boolean online = Common.currentUser.isOnline();
                                    if (status.equals("1") && user.isEmailVerified()) {
                                        if (!online) {
                                            SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
                                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                                            prefEditor.putInt("isLogged", 1);
                                            prefEditor.putString("isUser", "rider");
                                            prefEditor.putString("email", editEmail.getText().toString());
                                            prefEditor.putString("pass", editPassword.getText().toString());
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
                                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });

                                            startActivity(new Intent(MainActivity.this, RiderActivity.class));
                                            finish();
                                        } else {
                                            Snackbar.make(rootLayout, "Please logout the account from previous phone", Snackbar.LENGTH_LONG).show();
                                            btnSign.setEnabled(true);
                                        }
                                    } else {
                                        Snackbar.make(rootLayout, "Please verify your email", Snackbar.LENGTH_LONG).show();
                                        btnSign.setEnabled(true);
                                    }
                                } catch (Exception ex) {
                                    btnSign.setEnabled(true);

                                    users = db.getReference(Common.drivers);
                                    mListener = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(mListener);

                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                            try {
                                                String status = Common.currentUser.getIdentity();
                                                boolean online = Common.currentUser.isOnline();
                                                if (status.equals("0") && !online) {
                                                    SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
                                                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                    prefEditor.putInt("isLogged", 1);
                                                    prefEditor.putString("isUser", "driver");
                                                    prefEditor.putString("email", editEmail.getText().toString());
                                                    prefEditor.putString("pass", editPassword.getText().toString());
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
                                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                        }
                                                    });

                                                    startActivity(new Intent(MainActivity.this, VaresconDriverActivity.class));
                                                    finish();
                                                } else {
                                                    Snackbar.make(rootLayout, "Please logout the account from previous phone", Snackbar.LENGTH_LONG).show();
                                                    btnSign.setEnabled(true);
                                                }
                                            } catch (Exception ex) {

                                                btnSign.setEnabled(true);
                                                users = db.getReference("Admins");
                                                mListener = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(mListener);

                                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                                        try {
                                                            String status = Common.currentUser.getIdentity();
                                                            boolean online = Common.currentUser.isOnline();
                                                            if (status.equals("2") && !online) {
                                                                SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
                                                                SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                                prefEditor.putInt("isLogged", 1);
                                                                prefEditor.putString("isUser", "admin");
                                                                prefEditor.putString("email", editEmail.getText().toString());
                                                                Common.getAdminEmail = editEmail.getText().toString();
                                                                prefEditor.putString("pass", editPassword.getText().toString());
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
                                                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                    }
                                                                });

                                                                startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                                                finish();
                                                            } else {
                                                                Snackbar.make(rootLayout, "Please logout the account from previous phone", Snackbar.LENGTH_LONG).show();
                                                                btnSign.setEnabled(true);
                                                            }
                                                        } catch (Exception ex) {

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Log.e("Error","Something wrong");
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("Error","Something wrong");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, "Failed: Invalid Email Address/Password. Please Try Again", Snackbar.LENGTH_LONG).show();

                        //Activate Button
                        btnSign.setEnabled(true);
                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        users = db.getReference(Common.riders);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);
        final MaterialEditText editFirstname = register_layout.findViewById(R.id.editFirstname);
        final MaterialEditText editLastname = register_layout.findViewById(R.id.editLastname);

        editFirstname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editLastname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        dialog.setView(register_layout);

        InputFilter[] Textfilters = new InputFilter[1];
        Textfilters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {

                    char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

                    for (int index = start; index < end; index++) {
                        if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) {
                            return "";
                        }
                    }
                }
                return null;
            }

        };
        editFirstname.setFilters(Textfilters);
        editLastname.setFilters(Textfilters);


        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Email Address", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (editPassword.getText().length() < 6) {
                    Snackbar.make(rootLayout, "Password is too short. Please input at least 6 characters.", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter your First Name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editLastname.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter your Last Name", Snackbar.LENGTH_LONG).show();
                    return;
                }

                boolean indicator = isEmailValid(editEmail.getText().toString());
                if(!indicator){
                    Snackbar.make(rootLayout, "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                }

                final SweetAlertDialog waitingDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                waitingDialog.setTitleText("Creating User");
                waitingDialog.setCancelable(false);
                waitingDialog.show();
                //REGISTER NEW USER
                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                //SAVE USER TO DATABASE
                                User user = new User();
                                user.setEmail(editEmail.getText().toString());
                                user.setPassword(editPassword.getText().toString());
                                user.setFirstname(editFirstname.getText().toString());
                                user.setLastname(editLastname.getText().toString());
                                user.setOnline(false);
                                user.setIdentity("1");
                                user.setProfilePic("https://firebasestorage.googleapis.com/v0/b/varescon-186823.appspot.com/o/Riders%2FprofilePic.png?alt=media&token=d81d10cd-af61-480e-82ec-3d3b717dbf2a");


                                //ADD TO KEY
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                waitingDialog.dismiss();
                                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                        .setTitleText("Email Verification Sent")
                                                        .setContentText("Please Verify your Email to Log In")
                                                        .setConfirmText("OK")
                                                        .setConfirmClickListener(null)
                                                        .show();
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                user.sendEmailVerification();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                waitingDialog.dismiss();
                                                Snackbar.make(rootLayout, "Register Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }
}
