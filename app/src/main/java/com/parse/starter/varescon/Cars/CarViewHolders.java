package com.parse.starter.varescon.Cars;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.History.HistoryRecycler.HistorySinglePage;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.ReserveRide;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import static com.parse.starter.varescon.Cars.Car_information.refreshData;

/**
 * Created by iSwear on 1/7/2018.
 */

public class CarViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideIdCar;
    private String carId;
    private String carType;
    private String plate;
    private String plateNo;
    private FirebaseDatabase db;
    private DatabaseReference cars;
    private String carTypeItem;
    private String carStatusItem;
    private boolean newItem;
    private int i;
    private int carStatusPosition;


    public CarViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideIdCar = itemView.findViewById(R.id.rideIdCar);
    }

    @Override
    public void onClick(View v) {
        openEditDialog();
    }

    private void openEditDialog() {
        String rawId = rideIdCar.getText().toString();
        final String[] parts = rawId.split(" ");
        plate = parts[2];
        plateNo = parts[3];
        carType = parts[5]+ " " +parts[6] + " " + parts[7] + " " + parts[8] +" "+ parts[9];

        AlertDialog.Builder dialog = new AlertDialog.Builder(this.itemView.getContext());
        dialog.setTitle("CAR INFORMATION");
        dialog.setMessage("Add new car information");
        LayoutInflater inflater = LayoutInflater.from(this.itemView.getContext());
        View layout = inflater.inflate(R.layout.layout_add_cars, null);
        dialog.setView(layout);

        final MaterialEditText editPlate = layout.findViewById(R.id.editPlate);
        final MaterialEditText editNo = layout.findViewById(R.id.editNo);
        final MaterialSpinner selectCarType = layout.findViewById(R.id.selectCarType);
        final MaterialSpinner selectStatus = layout.findViewById(R.id.selectStatus);
        selectCarType.setItems("-Type of car -", "Sedan - 4 passenger seats", "Van - 8 passenger seats");
        selectStatus.setItems("-Car status-", "Available", "Not Available");


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
        Textfilters[0] = new InputFilter(){
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

        editPlate.setMaxCharacters(3);
        editPlate.setFilters(Textfilters);
        editPlate.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);


        editNo.setMaxCharacters(3);
        //editNo.setFilters(Numbers);
        editNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        editPlate.addTextChangedListener(tw);
        editNo.addTextChangedListener(tw);

        editPlate.setText(plate);
        editNo.setText(plateNo);

        selectCarType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carTypeItem = String.valueOf(item);
            }
        });

        selectStatus.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                carStatusItem = String.valueOf(item);
                carStatusPosition = position;
                if(carStatusItem == "Available"){
                    newItem = true;
                }
                else if(carStatusItem == "Not Available"){
                    newItem = false;
                }
            }
        });


        if(carType.equals("Sedan - 4 passenger seats")){
            selectCarType.setSelectedIndex(1);
            carTypeItem = "Sedan - 4 passenger seats";
        }
        else if(carType.equals("Van - 8 passenger seats")){
            selectCarType.setSelectedIndex(2);
            carTypeItem = "Van - 8 passenger seats";
        }
        /*if(parts[11].equals("true")){
            selectStatus.setSelectedIndex(1);
            carStatusItem = "true";
        }
        else {
            selectStatus.setSelectedIndex(2);
            carStatusItem = "false";
        }*/



        dialog.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(editPlate.getText().toString())) {
                    Snackbar.make(itemView, "Please enter driver's first name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(editNo.getText().toString())) {
                    Snackbar.make(itemView, "Please enter driver's last name", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(selectStatus.getSelectedIndex() == 0){
                    Snackbar.make(itemView, "Please enter the car's status", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (editPlate.getText().toString().length() > 3 || editNo.getText().toString().length() > 3) {
                    Snackbar.make(itemView, "Character exceeded to limit!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (editPlate.getText().toString().length() < 3 || editNo.getText().toString().length() < 3) {
                    Snackbar.make(itemView, "Character is less than 3. Please try again", Snackbar.LENGTH_LONG).show();
                    return;
                }


                if(i > 2){
                    DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference("Cars");
                    userHistoryRef.orderByChild("plate_no").equalTo(editPlate.getText().toString().toUpperCase() + " " + editNo.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Snackbar.make(itemView, "Plate number already exist", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            else{
                                db = FirebaseDatabase.getInstance();
                                cars = db.getReference("Cars");

                                cars.child(parts[13]).child("plate_no").setValue(editPlate.getText().toString().toUpperCase() + " " + editNo.getText().toString());
                                cars.child(parts[13]).child("status").setValue(newItem);
                                dialog.dismiss();

                                new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Successfully edited")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                refreshData();
                                                sweetAlertDialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    db = FirebaseDatabase.getInstance();
                    cars = db.getReference("Cars");

                    cars.child(parts[13]).child("plate_no").setValue(editPlate.getText().toString().toUpperCase() + " " + editNo.getText().toString());
                    cars.child(parts[13]).child("status").setValue(newItem);
                    dialog.dismiss();

                    new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success")
                            .setContentText("Successfully edited")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    refreshData();
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        selectCarType.setVisibility(View.GONE);
        dialog.show();
    }
}
