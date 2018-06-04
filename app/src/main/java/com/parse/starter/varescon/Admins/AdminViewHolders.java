package com.parse.starter.varescon.Admins;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Cars.Car_information;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.parse.starter.varescon.Admins.Admin_information.refreshData;

/**
 * Created by iSwear on 1/7/2018.
 */

public class AdminViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideIdCar;
    private FirebaseAuth auth;
    private int i;


    public AdminViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideIdCar = itemView.findViewById(R.id.rideIdCar);
    }

    @Override
    public void onClick(View v){
        auth = FirebaseAuth.getInstance();
        String[] split = rideIdCar.getText().toString().split(" ");
        final String adminId = split[1];

        AlertDialog.Builder dialog = new AlertDialog.Builder(itemView.getContext());
        dialog.setTitle("LOGIN");
        dialog.setMessage("Please re-enter current user's login password");

        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View register_layout = inflater.inflate(R.layout.layout_admin_resignin, null);

        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);

        dialog.setView(register_layout);

        dialog.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(itemView, "Please Enter Email Address", Snackbar.LENGTH_LONG).show();
                    return;
                }


                final SweetAlertDialog waitingDialog = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                waitingDialog.setTitleText("Re-authenticating");
                waitingDialog.setCancelable(false);
                waitingDialog.show();
                auth.signInWithEmailAndPassword(Common.getAdminEmail, editPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        showAddDialog(adminId);
                        waitingDialog.dismiss();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(itemView, "Failed: Invalid password. Please Try Again", Snackbar.LENGTH_LONG).show();
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

    private void showAddDialog(final String adminId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(itemView.getContext());
        dialog.setTitle("LOGIN");
        dialog.setMessage("Please re-enter current user's login password");

        LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
        View register_layout = inflater.inflate(R.layout.layout_add_admin_edit, null);

        final MaterialEditText editFirstname = register_layout.findViewById(R.id.editFirstname);
        final MaterialEditText editLastname = register_layout.findViewById(R.id.editLastname);

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Admins").child(adminId);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    editFirstname.setText(dataSnapshot.child("firstname").getValue().toString());
                    editLastname.setText(dataSnapshot.child("lastname").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        i = 0;
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                i++;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
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
        editFirstname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editLastname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editFirstname.addTextChangedListener(tw);
        editLastname.addTextChangedListener(tw);

        dialog.setView(register_layout);

        dialog.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                    Snackbar.make(itemView, "Please enter the first name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editLastname.getText().toString())) {
                    Snackbar.make(itemView, "Please enter the last name", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(i > 2){
                    DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Admins");
                    userHistoryRef.orderByChild("firstname").equalTo(editFirstname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Admins");
                                userHistoryRef.orderByChild("lastname").equalTo(editLastname.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Snackbar.make(itemView, "Admin already exist", Snackbar.LENGTH_LONG).show();
                                            return;
                                        } else {
                                            final SweetAlertDialog loadingbar = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                                            loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                                            loadingbar.setTitleText("Updating admin information");
                                            loadingbar.setCancelable(false);
                                            loadingbar.show();

                                            FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("firstname").setValue(editFirstname.getText().toString());
                                            FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("lastname").setValue(editLastname.getText().toString());
                                            new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Success")
                                                    .setContentText("Successfully edited an admin")
                                                    .setConfirmText("OK")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            refreshData();
                                                            sweetAlertDialog.dismiss();
                                                        }
                                                    })
                                                    .show();
                                            loadingbar.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                final SweetAlertDialog loadingbar = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                                loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                                loadingbar.setTitleText("Updating admin information");
                                loadingbar.setCancelable(false);
                                loadingbar.show();

                                FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("firstname").setValue(editFirstname.getText().toString());
                                FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("lastname").setValue(editLastname.getText().toString());
                                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Successfully edited an admin")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                refreshData();
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .show();
                                loadingbar.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    final SweetAlertDialog loadingbar = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.PROGRESS_TYPE);
                    loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                    loadingbar.setTitleText("Updating admin information");
                    loadingbar.setCancelable(false);
                    loadingbar.show();

                    FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("firstname").setValue(editFirstname.getText().toString());
                    FirebaseDatabase.getInstance().getReference("Admins").child(adminId).child("lastname").setValue(editLastname.getText().toString());
                    new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success")
                            .setContentText("Successfully edited an admin")
                            .setConfirmText("OK")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    refreshData();
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                    loadingbar.dismiss();
                }


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
}
