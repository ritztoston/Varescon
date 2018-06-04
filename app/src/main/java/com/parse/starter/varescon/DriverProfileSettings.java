package com.parse.starter.varescon;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverProfileSettings extends Fragment {

    FrameLayout rootLayout;

    TextView riderName;
    Button changeName, changePassword;
    String userEmail, userUrl;

    CircleImageView profile_Pic;
    private static final int PICK_IMAGE = 1;
    private Uri filePath;

    FirebaseDatabase db;
    DatabaseReference users;
    StorageReference mStorage;
    private ValueEventListener mListener;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        riderName = getView().findViewById(R.id.settingsRiderName);
        rootLayout = getView().findViewById(R.id.rootLayout_settings);
        changeName = getView().findViewById(R.id.changeNameBtn);
        changePassword = getView().findViewById(R.id.changePassBtn);
        profile_Pic = getView().findViewById(R.id.profilePic);

        FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Common.currentUser = dataSnapshot.getValue(User.class);
                    riderName.setText("Name: "+Common.currentUser.getFirstname()+" "+Common.currentUser.getLastname());
                    userUrl = Common.currentUser.getProfilePic().toString();
                    //SET PROFILE PICTURE
                    Glide
                            .with(getContext())
                            .load(userUrl)
                            .dontAnimate()
                            .into(profile_Pic);
                } catch (Exception ex)
                {
                    //TODO
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.drivers);

        mListener = users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(mListener);

        mStorage = FirebaseStorage.getInstance().getReference();

        profile_Pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("CHANGE NAME");

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View changeName = inflater.inflate(R.layout.change_name, null);
                dialog.setView(changeName);

                final MaterialEditText editFirstname = changeName.findViewById(R.id.settingsEditFirst);
                final MaterialEditText editLastname = changeName.findViewById(R.id.settingsEditLast);

                editFirstname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editLastname.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
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

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                Common.currentUser = dataSnapshot.getValue(User.class);
                                editFirstname.setText(Common.currentUser.getFirstname());
                                editLastname.setText(Common.currentUser.getLastname());
                            } catch (Exception ex) {
                                //TODO
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                dialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (TextUtils.isEmpty(editFirstname.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter your First Name", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(editLastname.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter your Last Name", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        final SweetAlertDialog waitingDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                        waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                        waitingDialog.setTitleText("Updating");
                        waitingDialog.setCancelable(false);
                        waitingDialog.show();
                            final String firstname = editFirstname.getText().toString();
                            final String lastname = editLastname.getText().toString();

                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("firstname").setValue(firstname).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lastname").setValue(lastname).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Success: Updated Profile Name", Toast.LENGTH_LONG).show();
                                            waitingDialog.dismiss();
                                        }
                                    });
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
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                Common.currentUser = dataSnapshot.getValue(User.class);
                                userEmail = Common.currentUser.getEmail();
                            } catch (Exception ex) {
                                //TODO
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("USER RE-AUTHETICATION");


                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View reauthenticate = inflater.inflate(R.layout.reauthenticate, null);
                dialog.setView(reauthenticate);

                final MaterialEditText editPassword = reauthenticate.findViewById(R.id.editPassword);
                final MaterialEditText newEditPassword = reauthenticate.findViewById(R.id.newEditPassword);
                final MaterialEditText reEditPassword = reauthenticate.findViewById(R.id.reEditPassword);

                dialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (TextUtils.isEmpty(editPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(newEditPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if(newEditPassword.getText().length() < 6)
                        {
                            Snackbar.make(rootLayout,"Password is too short. Please input at least 6 characters.",Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(reEditPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        final String newPass = newEditPassword.getText().toString();
                        final String confirmPass = reEditPassword.getText().toString();
                        if (newPass.equals(confirmPass)) {

                            final SweetAlertDialog waitingDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                            waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                            waitingDialog.setTitleText("Updating");
                            waitingDialog.setCancelable(false);
                            waitingDialog.show();

                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, editPassword.getText().toString());
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    SharedPreferences sharedPref = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                                                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                                                    prefEditor.putString("pass", newPass);
                                                    prefEditor.commit();
                                                    Toast.makeText(getActivity(), "Success: Updated Password", Toast.LENGTH_LONG).show();
                                                    waitingDialog.dismiss();
                                                }
                                                else {
                                                    Toast.makeText(getActivity(), "Failed: Password not updated", Toast.LENGTH_LONG).show();
                                                    waitingDialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Snackbar.make(rootLayout, "Error: Old password is invalid", Snackbar.LENGTH_LONG).show();
                                        waitingDialog.dismiss();
                                    }
                                }
                            });
                        } else
                            Snackbar.make(rootLayout, "Error: Password do not match", Snackbar.LENGTH_LONG).show();
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
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rider_profile_settings, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();

            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),filePath);
                profile_Pic.setImageBitmap(bitmap);
                StorageReference mChildStorage = mStorage.child(Common.drivers).child(filePath.getLastPathSegment());
                final SweetAlertDialog waitingDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                waitingDialog.getProgressHelper().setBarColor(Color.parseColor("#F71735"));
                waitingDialog.setTitleText("Updating");
                waitingDialog.setCancelable(false);
                waitingDialog.show();


                mChildStorage.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String value = downloadUrl.toString();

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profilePic").setValue(value)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                waitingDialog.dismiss();
                                Toast.makeText(getActivity(), "Success: Updated Profile Picture", Toast.LENGTH_LONG).show();

                                Glide
                                        .with(getContext())
                                        .load(downloadUrl.toString())
                                        .centerCrop()
                                        .into(profile_Pic);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toast.makeText(getActivity(), "Failed: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });


            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
