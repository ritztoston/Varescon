package com.parse.starter.varescon.DriverReportsRecycler;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Cars.CarAdapter;
import com.parse.starter.varescon.Cars.CarsObject;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.VaresconDriverActivity;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriversReportInformation extends Fragment {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView mHistoryRecyclerView;
    private DriverReportAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private ArrayList resultsHistory = new ArrayList<DriverReportObject>();

    Button generateReport;
    MaterialSpinner dateReport, yearReport;
    TextView noResults;

    String month;
    String year;

    String driverName;
    int itemList;
    ArrayList<String> rideID;
    ArrayList<String> amtPaid;
    ArrayList<String> driverSal;
    ArrayList<String> rideDate;
    Double salaryFinal = 0.0;

    private ArrayList<Double> salaryDriver;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noResults = getView().findViewById(R.id.noResults);
        searchText = getView().findViewById(R.id.searchText);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new DriverReportAdapter(getDataSetHistory(), getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        generateReport = getView().findViewById(R.id.generateReport);
        dateReport = getView().findViewById(R.id.dateReport);
        yearReport = getView().findViewById(R.id.yearReport);

        dateReport.setItems("Month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        yearReport.setItems("Year", "2018", "2019", "2020", "2021", "2022", "2023");
        verifyStoragePermissions(getActivity());
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


        getDriverInfo();
        dateReport.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                month = String.valueOf(item);
                if (month != null && year != null) {
                    if (month != "Month" && year != "Year") {
                        noResults.setVisibility(View.GONE);
                        generateReport.setEnabled(true);
                        generateReport.setBackgroundResource(R.drawable.signin_button_design);
                        getReport();
                    } else {
                        generateReport.setEnabled(false);
                        generateReport.setBackgroundResource(R.drawable.gray_out_design);
                        mHistoryAdapter.clear();
                        resultsHistory.clear();
                        noResults.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        yearReport.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                year = String.valueOf(item);
                if (month != null && year != null) {
                    if (month != "Month" && year != "Year") {
                        noResults.setVisibility(View.GONE);
                        generateReport.setEnabled(true);
                        generateReport.setBackgroundResource(R.drawable.signin_button_design);
                        getReport();
                    } else {
                        generateReport.setEnabled(false);
                        generateReport.setBackgroundResource(R.drawable.gray_out_design);
                        mHistoryAdapter.clear();
                        resultsHistory.clear();
                        noResults.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPdf(v);
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Success")
                        .setContentText("Generated report")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        });


    }

    private void getDriverInfo() {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fname = dataSnapshot.child("firstname").getValue().toString();
                    String lastname = dataSnapshot.child("lastname").getValue().toString();
                    driverName = fname + " " + lastname;
                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createPdf(View v) {
        salaryFinal = 0.0;
        final Document doc = new Document();
        String outPath = Environment.getExternalStorageDirectory() + "/monthly_salary.pdf";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = df.format(c.getTime());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(outPath));
            doc.open();

            doc.setMargins(0f, 0f, 10f, 0f);
            Paragraph p = new Paragraph(new Phrase(10f, "Varescon Transport Services Company", FontFactory.getFont(FontFactory.COURIER, 23f)));
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph(new Phrase(25f, "San Jose Street, Tagbilaran City, 6300 Bohol", FontFactory.getFont(FontFactory.COURIER, 15f)));
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph(new Phrase(27f, "(038) 412 2507", FontFactory.getFont(FontFactory.COURIER, 18f)));
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph(new Phrase(20f, "Date Generated: " + formattedDate, FontFactory.getFont(FontFactory.COURIER, 12f)));
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph(new Phrase(20f, "Date Report: " + month + " Year: " + year, FontFactory.getFont(FontFactory.COURIER, 12f)));
            p.setAlignment(Element.ALIGN_CENTER);

            doc.add(p);

            final PdfPTable table = new PdfPTable(4);

            table.setPaddingTop(10);
            PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(25f, "Driver Name: " + driverName, FontFactory.getFont(FontFactory.COURIER, 18f))));
            cell.setPadding(10);
            cell.setColspan(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);


            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Date", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Ride ID", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(15f, "Amount Customer Paid", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(15f, "Salary Per Ride", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);


            for (int i = 0; i < itemList; i++) {
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(25f, "" + rideDate.get(i))));
                    cell.setPadding(10);
                    table.addCell(cell);
                } catch (Exception e) {
                    table.addCell(new Paragraph(new Phrase(15f, "NULL", FontFactory.getFont(FontFactory.COURIER, 15f))));
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(25f, "" + rideID.get(i))));
                    cell.setPadding(10);
                    table.addCell(cell);
                } catch (Exception e) {
                    table.addCell(new Paragraph(new Phrase(15f, "NULL", FontFactory.getFont(FontFactory.COURIER, 15f))));
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "" + amtPaid.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);

                } catch (Exception e) {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P 0", FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P " + driverSal.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);

                } catch (Exception e) {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P 0", FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                }
                try {
                    Double sal = Double.parseDouble(driverSal.get(i));
                    Log.e("salary", String.valueOf(sal));
                    salaryFinal = salaryFinal + sal;
                } catch (Exception e) {
                }
            }

            table.setPaddingTop(10);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Total Month Salary: " + "P " + salaryFinal, FontFactory.getFont(FontFactory.COURIER, 15f))));
            cell.setPadding(10);
            cell.setColspan(4);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);

            doc.add(table);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            doc.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void getReport() {
        mHistoryAdapter.clear();
        resultsHistory.clear();
        itemList = 0;

        rideID = new ArrayList<>();
        amtPaid = new ArrayList<>();
        driverSal = new ArrayList<>();
        rideDate = new ArrayList<>();
        salaryDriver = new ArrayList<>();

        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchRideInfo(d.getKey());
                    }

                } else {
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInfo(final String key) {

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue().toString().equals("reserve")) {
                        Log.e("reserve","called");
                        if (dataSnapshot.child("progress").getValue().toString().equals("true")) {
                            Log.e("progress","called");
                            String[] date = dataSnapshot.child("date").getValue().toString().split(" ");
                            String monthD = date[0];
                            String yearD = date[2];
                            if (dataSnapshot.child("driver").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && monthD.equals(month) && yearD.equals(year)) {
                                Log.e("asd", "called");
                                itemList++;
                                String rideId = key;
                                rideID.add(key);


                                String amt_paid;

                                try {
                                    amt_paid = dataSnapshot.child("amt_paid").getValue().toString();
                                    amtPaid.add(amt_paid);
                                } catch (Exception e) {
                                    amt_paid = "P 0";
                                }
                                String salary;
                                try {
                                    salary = dataSnapshot.child("driver_ride_salary").getValue().toString();
                                    salaryDriver.add(Double.parseDouble(String.valueOf(dataSnapshot.child("driver_ride_salary").getValue())));
                                    driverSal.add(salary);
                                } catch (Exception e) {
                                    salary = "0";
                                }
                                String finalDate = dataSnapshot.child("date").getValue().toString();
                                rideDate.add(finalDate);
                                DriverReportObject obj = new DriverReportObject(rideId, amt_paid, salary, finalDate);
                                resultsHistory.add(obj);

                                mHistoryAdapter.notifyDataSetChanged();
                                if (rideID.size() == 0) {
                                    noResults.setVisibility(View.VISIBLE);
                                    generateReport.setEnabled(false);
                                    generateReport.setBackgroundResource(R.drawable.gray_out_design);
                                }
                            }
                            if (rideID.size() == 0) {
                                noResults.setVisibility(View.VISIBLE);
                                generateReport.setEnabled(false);
                                generateReport.setBackgroundResource(R.drawable.gray_out_design);
                            } else {
                                noResults.setVisibility(View.GONE);
                                generateReport.setEnabled(true);
                                generateReport.setBackgroundResource(R.drawable.signin_button_design);
                            }
                        }
                    } else {
                        String[] date = dataSnapshot.child("date").getValue().toString().split(" ");
                        String monthD = date[0];
                        String yearD = date[2];
                        try {
                            if (dataSnapshot.child("driver").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && monthD.equals(month) && yearD.equals(year)) {
                                Log.e("asd", "called");
                                itemList++;
                                String rideId = key;
                                rideID.add(key);


                                String amt_paid;

                                try {
                                    amt_paid = dataSnapshot.child("amt_paid").getValue().toString();
                                    amtPaid.add(amt_paid);
                                } catch (Exception e) {
                                    amt_paid = "P 0";
                                }
                                String salary;
                                try {
                                    salary = dataSnapshot.child("driver_ride_salary").getValue().toString();
                                    salaryDriver.add(Double.parseDouble(String.valueOf(dataSnapshot.child("driver_ride_salary").getValue())));
                                    driverSal.add(salary);
                                } catch (Exception e) {
                                    salary = "0";
                                }
                                String finalDate = dataSnapshot.child("date").getValue().toString();
                                rideDate.add(finalDate);
                                DriverReportObject obj = new DriverReportObject(rideId, amt_paid, salary, finalDate);
                                resultsHistory.add(obj);

                                mHistoryAdapter.notifyDataSetChanged();
                                if (rideID.size() == 0) {
                                    noResults.setVisibility(View.VISIBLE);
                                    generateReport.setEnabled(false);
                                    generateReport.setBackgroundResource(R.drawable.gray_out_design);
                                }
                            }
                        }catch (Exception e){}
                        if (rideID.size() == 0) {
                            noResults.setVisibility(View.VISIBLE);
                            generateReport.setEnabled(false);
                            generateReport.setBackgroundResource(R.drawable.gray_out_design);
                        } else {
                            noResults.setVisibility(View.GONE);
                            generateReport.setEnabled(true);
                            generateReport.setBackgroundResource(R.drawable.signin_button_design);
                        }
                    }

                } else {
                    noResults.setVisibility(View.VISIBLE);
                    generateReport.setEnabled(false);
                    generateReport.setBackgroundResource(R.drawable.gray_out_design);
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
        return inflater.inflate(R.layout.fragment_driver_reports, container, false);
    }

    private ArrayList<DriverReportObject> getDataSetHistory() {
        return resultsHistory;
    }
}
