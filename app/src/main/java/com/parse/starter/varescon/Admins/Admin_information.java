package com.parse.starter.varescon.Admins;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.parse.starter.varescon.AdminActivity;
import com.parse.starter.varescon.Cars.CarAdapter;
import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Drivers.DriverAdapter;
import com.parse.starter.varescon.MainActivity;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.RiderActivity;
import com.parse.starter.varescon.VaresconDriverActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class Admin_information extends Fragment {

    private RecyclerView mHistoryRecyclerView;
    private static AdminAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private TextView noResults;
    private Button addAdmin, generateCode;
    private static ArrayList resultsHistory = new ArrayList<AdminObject>();
    private FirebaseDatabase db;
    private DatabaseReference ccode;
    private FirebaseAuth auth;
    private DatabaseReference users;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Admins");
        searchText = getView().findViewById(R.id.searchText);
        noResults = getView().findViewById(R.id.noResults);
        addAdmin = getView().findViewById(R.id.addAdmin);
        generateCode = getView().findViewById(R.id.generateCode);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new AdminAdapter(getDataSetHistory(), getView());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        addAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDialog();
            }
        });

        generateCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Are you sure to generate cancellation code for drivers?")
                        .setConfirmText("CONFIRM").setCancelText("CANCEL")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                FirebaseDatabase.getInstance().getReference("ConfirmationCode").removeValue();
                                db = FirebaseDatabase.getInstance();
                                ccode = db.getReference("ConfirmationCode");
                                String requestId = ccode.push().getKey();
                                ccode.child(requestId).setValue(false);
                                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Cancellation Code")
                                        .setContentText(requestId)
                                        .setConfirmText("CONFIRM")
                                        .setConfirmClickListener(null)
                                        .show();


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
        });


        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mHistoryAdapter.getFilter().filter(s.toString());
            }
        });



        try{
            refreshData();
        }catch (Exception e){
            getAdminInfo();
        }

    }

    private void openAddDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("LOGIN");
        dialog.setMessage("Please re-enter current user's login password");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View register_layout = inflater.inflate(R.layout.layout_admin_resignin, null);

        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);

        dialog.setView(register_layout);

        dialog.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(getView(), "Please enter your password", Snackbar.LENGTH_LONG).show();
                    return;
                }


                final SweetAlertDialog waitingDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                waitingDialog.setTitleText("Re-authenticating");
                waitingDialog.setCancelable(false);
                waitingDialog.show();
                auth.signInWithEmailAndPassword(Common.getAdminEmail, editPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        showAddDialog();
                        waitingDialog.dismiss();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(getView(), "Failed: Invalid password. Please Try Again", Snackbar.LENGTH_LONG).show();
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

    private void showAddDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("ADMINISTRATOR");
        dialog.setMessage("Create new admin user");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View register_layout = inflater.inflate(R.layout.layout_add_admin, null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);
        final MaterialEditText editFirstname = register_layout.findViewById(R.id.editFirstname);
        final MaterialEditText editLastname = register_layout.findViewById(R.id.editLastname);

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
        editPassword.setMinCharacters(6);


        dialog.setView(register_layout);

        dialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(getView(), "Please enter Email Address", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(getView(), "Please enter the password", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(editPassword.getText().toString().length() < 6){
                    Snackbar.make(getView(), "Password is too short!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                    Snackbar.make(getView(), "Please enter the first name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editLastname.getText().toString())) {
                    Snackbar.make(getView(), "Please enter the last name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                boolean indicator = isEmailValid(editEmail.getText().toString());
                if (!indicator) {
                    Snackbar.make(getView(), "Invalid email address", Snackbar.LENGTH_LONG).show();
                    return;
                }
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
                                        Snackbar.make(getView(), "Admin already exist", Snackbar.LENGTH_LONG).show();
                                        return;
                                    }
                                    else{
                                        final SweetAlertDialog loadingbar = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                                        loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                                        loadingbar.setTitleText("Adding a new admin user");
                                        loadingbar.setCancelable(false);
                                        loadingbar.show();
                                        auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                    @Override
                                                    public void onSuccess(AuthResult authResult) {

                                                        HashMap map = new HashMap();
                                                        map.put("email", editEmail.getText().toString());
                                                        map.put("firstname", editFirstname.getText().toString());
                                                        map.put("lastname", editLastname.getText().toString());
                                                        map.put("online", false);
                                                        map.put("identity", "2");
                                                        map.put("profilePic", "https://firebasestorage.googleapis.com/v0/b/varescon-186823.appspot.com/o/Riders%2FprofilePic.png?alt=media&token=d81d10cd-af61-480e-82ec-3d3b717dbf2a");


                                                        final String driverKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                        //ADD TO KEY
                                                        users.child(driverKey)
                                                                .setValue(map)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        loadingbar.dismiss();
                                                                        new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                                                                .setTitleText("Success")
                                                                                .setContentText("Successfully added an admin")
                                                                                .setConfirmText("OK")
                                                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                                    @Override
                                                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                        refreshData();
                                                                                        sweetAlertDialog.dismiss();
                                                                                    }
                                                                                })
                                                                                .show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        loadingbar.dismiss();
                                                                        Snackbar.make(getView(), "Add Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingbar.dismiss();
                                                Snackbar.make(getView(), "Add Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else{
                            final SweetAlertDialog loadingbar = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                            loadingbar.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                            loadingbar.setTitleText("Adding a new admin user");
                            loadingbar.setCancelable(false);
                            loadingbar.show();
                            auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {

                                            HashMap map = new HashMap();
                                            map.put("email", editEmail.getText().toString());
                                            map.put("firstname", editFirstname.getText().toString());
                                            map.put("lastname", editLastname.getText().toString());
                                            map.put("online", false);
                                            map.put("identity", "2");
                                            map.put("profilePic", "https://firebasestorage.googleapis.com/v0/b/varescon-186823.appspot.com/o/Riders%2FprofilePic.png?alt=media&token=d81d10cd-af61-480e-82ec-3d3b717dbf2a");


                                            final String driverKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            //ADD TO KEY
                                            users.child(driverKey)
                                                    .setValue(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            loadingbar.dismiss();
                                                            new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                                                                    .setTitleText("Success")
                                                                    .setContentText("Successfully added an admin")
                                                                    .setConfirmText("OK")
                                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                        @Override
                                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                            refreshData();
                                                                            sweetAlertDialog.dismiss();
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            loadingbar.dismiss();
                                                            Snackbar.make(getView(), "Add Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingbar.dismiss();
                                    Snackbar.make(getView(), "Add Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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

    private static void getAdminInfo() {
        Log.e("sss","called");
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Admins");
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchRideInfo(d.getKey());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void FetchRideInfo(String key) {

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Admins").child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        String email = String.valueOf(dataSnapshot.child("email").getValue());
                        String fname = String.valueOf(dataSnapshot.child("firstname").getValue());
                        String lname = String.valueOf(dataSnapshot.child("lastname").getValue());

                        AdminObject obj = new AdminObject(email, fname+" "+lname, dataSnapshot.getKey());
                        resultsHistory.add(obj);
                        mHistoryAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_information, container, false);
    }

    private ArrayList<AdminObject> getDataSetHistory() {
        return resultsHistory;
    }

    public static void refreshData(){
        mHistoryAdapter.clear();
        resultsHistory.clear();
        getAdminInfo();
    }

    public boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.matches())
            return true;
        else
            return false;
    }
}
