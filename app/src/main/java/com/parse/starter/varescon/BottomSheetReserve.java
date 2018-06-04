package com.parse.starter.varescon;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by iSwear on 12/6/2017.
 */

public class BottomSheetReserve extends BottomSheetDialogFragment {
    String mLocation, mDestination;

    private boolean secondDes;

    IGoogleAPI mService;

    TextView textCalculate, textLocation, textDestination;
    private boolean isTapOnMap;

    public static BottomSheetReserve newInstance(String location, String destination, boolean isTapOnMap, boolean secondDes) {
        BottomSheetReserve f = new BottomSheetReserve();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);
        args.putBoolean("isTapOnMap", isTapOnMap);
        args.putBoolean("secondDes", secondDes);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
        secondDes = getArguments().getBoolean("secondDes");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_reserve, container, false);

        textLocation = view.findViewById(R.id.textLocationR);
        textDestination = view.findViewById(R.id.textDestinationR);
        textCalculate = view.findViewById(R.id.textCalculateR);

        mService = Common.getGoogleService();
        getPrice(mLocation, mDestination);

        //SET DATA
        if (!isTapOnMap) {
            textLocation.setText(mLocation);
            textDestination.setText(mDestination);
        }
        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestUrl;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + mLocation + "&" +
                    "destination=" + mDestination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);


            Log.e("LINK", requestUrl);
            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        if(!secondDes) {
                            Double distance_value;
                            String distance_text;

                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            JSONArray routes = jsonObject.getJSONArray("routes");

                            JSONObject object = routes.getJSONObject(0);
                            JSONArray legs = object.getJSONArray("legs");

                            JSONObject legsObject = legs.getJSONObject(0);

                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            JSONObject distance = legsObject.getJSONObject("distance");

                            distance_text = distance.getString("text");
                            if (distance_text.contains("km"))
                                distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));
                            else {
                                Double rawDistance = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));
                                distance_value = rawDistance * 0.001;
                            }


                            //GET TIME
                            JSONObject time = legsObject.getJSONObject("duration");
                            String time_text = time.getString("text");
                            Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                            String final_calculate = String.format("%s + %s = P %.2f", distance_text, time_text,
                                    (Common.getPrice(distance_value, time_value)+(Common.getPrice(distance_value, time_value)*Common.reserveFee)));

                            textCalculate.setText(final_calculate);

                            if (isTapOnMap) {
                                String restrict_cebu = "cebu", restrict_road = "unnamed";
                                if (end_address.toLowerCase().contains(restrict_cebu.toLowerCase())) {
                                    if (this != null) {
                                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Error")
                                                .setContentText("Invalid route destination")
                                                .setConfirmText("OK")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        if (RiderActivity.greyPolyLine != null)
                                                            RiderActivity.greyPolyLine.remove();
                                                        if (RiderActivity.markerDestination != null)
                                                            RiderActivity.markerDestination.remove();
                                                        RiderActivity.isDestinationSet = false;
                                                        RiderActivity.firstClick = 0;
                                                        sweetAlertDialog.dismiss();
                                                    }
                                                })
                                                .show();
                                        dismiss();
                                    }
                                } else {
                                    textLocation.setText(start_address);
                                    textDestination.setText(end_address);
                                }
                            }

                            Common.startAdd = start_address;
                            Common.endAdd = end_address;
                            Common.reserveTimeValue = time_value;
                            Common.firstFare = distance_value;
                            Common.distanceText = distance_text;
                            Common.timeText = time_text;
                        }
                        else{
                            Double distance_value;
                            String distance_text;

                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            JSONArray routes = jsonObject.getJSONArray("routes");

                            JSONObject object = routes.getJSONObject(0);
                            JSONArray legs = object.getJSONArray("legs");

                            JSONObject legsObject = legs.getJSONObject(0);

                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            JSONObject distance = legsObject.getJSONObject("distance");

                            distance_text = distance.getString("text");
                            if (distance_text.contains("km"))
                                distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));
                            else {
                                Double rawDistance = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));
                                distance_value = rawDistance * 0.001;
                            }


                            //GET TIME
                            JSONObject time = legsObject.getJSONObject("duration");
                            String time_text = time.getString("text");


                            Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));






                            String final_calculate = String.format("Destination 1: %s + %s = P %.2f\nDestination 2: %s + %s = P %.2f\nTotal Service Amount: P %.2f",
                                    Common.distanceText, Common.timeText,
                                    Common.getPrice(Common.firstFare, Common.reserveTimeValue),

                                    distance_text, time_text,
                                    Common.getPrice(distance_value, time_value),

                                    (Common.getPrice(Common.firstFare, Common.reserveTimeValue)+Common.getPrice(distance_value, time_value)) + ((Common.getPrice(Common.firstFare, Common.reserveTimeValue)+Common.getPrice(distance_value, time_value))*Common.reserveFee));

                            textCalculate.setText(final_calculate);

                            if (isTapOnMap) {
                                String restrict_cebu = "cebu", restrict_road = "unnamed";
                                if (end_address.toLowerCase().contains(restrict_cebu.toLowerCase())) {
                                    if (this != null) {
                                        new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Error")
                                                .setContentText("Invalid route destination")
                                                .setConfirmText("OK")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        if (RiderActivity.greyPolyLine != null)
                                                            RiderActivity.greyPolyLine.remove();
                                                        if (RiderActivity.markerDestination != null)
                                                            RiderActivity.markerDestination.remove();
                                                        RiderActivity.isDestinationSet = false;
                                                        RiderActivity.firstClick = 0;
                                                        sweetAlertDialog.dismiss();
                                                    }
                                                })
                                                .show();
                                        dismiss();
                                    }
                                } else {
                                    textLocation.setText(Common.startAdd);
                                    textDestination.setText(String.format("Destination 1: %s\nDestination 2: %s",Common.endAdd,end_address));
                                }

                                Common.startAddTwo = start_address;
                                Common.endAddTwo = end_address;
                                Common.reserveTimeValueTwo = time_value;
                                Common.firstFareTwo = distance_value;
                                Common.distanceTextTwo = distance_text;
                                Common.timeTextTwo = time_text;
                            }
                        }


                    } catch (JSONException e) {
                        if (this != null) {
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Invalid route destination")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            if (RiderActivity.greyPolyLine != null)
                                                RiderActivity.greyPolyLine.remove();
                                            if (RiderActivity.markerDestination != null)
                                                RiderActivity.markerDestination.remove();
                                            sweetAlertDialog.dismiss();
                                        }
                                    })
                                    .show();
                            dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR", t.getMessage());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
