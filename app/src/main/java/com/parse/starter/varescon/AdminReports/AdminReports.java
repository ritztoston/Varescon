package com.parse.starter.varescon.AdminReports;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.parse.starter.varescon.Common.Common;
import com.parse.starter.varescon.DriverReportsRecycler.DriverReportAdapter;
import com.parse.starter.varescon.DriverReportsRecycler.DriverReportObject;
import com.parse.starter.varescon.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminReports extends Fragment {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView mHistoryRecyclerView;
    private AdminReportAdapter mHistoryAdapter;
    private LinearLayoutManager mHistoryLayoutManager;
    private MaterialEditText searchText;
    private ArrayList resultsHistory = new ArrayList<DriverReportObject>();

    Button generateReport;
    MaterialSpinner yearReport, monthReport;
    TextView noResults;

    String year;

    int itemList;
    ArrayList<String> rideID;
    ArrayList<String> amtPaid;
    ArrayList<String> driverSal;
    ArrayList<String> rideDate;

    private ArrayList<Double> salaryDriver;
    private ArrayList<String> earnings;
    private Double earning;
    private Double earningFinals = 0.0;
    private ArrayList<String> driverName;
    private String month;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        monthReport = getView().findViewById(R.id.monthReport);
        noResults = getView().findViewById(R.id.noResults);
        searchText = getView().findViewById(R.id.searchText);
        mHistoryRecyclerView = view.findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mHistoryLayoutManager.setReverseLayout(true);
        mHistoryLayoutManager.setStackFromEnd(true);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new AdminReportAdapter(getDataSetHistory(), getContext());
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        generateReport = getView().findViewById(R.id.generateReport);
        yearReport = getView().findViewById(R.id.yearReport);
        generateReport.setEnabled(false);
        generateReport.setBackgroundResource(R.drawable.gray_out_design);

        monthReport.setItems("Month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
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

        //getReportFirst();
        monthReport.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                month = String.valueOf(item);
                if (month != null && year != null) {
                    if (month != "Month" && year != "Year") {
                        noResults.setVisibility(View.GONE);
                        generateReport.setEnabled(true);
                        generateReport.setBackgroundResource(R.drawable.signin_button_design);
                        getReport();
                    } else if(month == "Month" && year == "Year"){

                    }else {
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
                if (year != null) {
                    if (month != "Month" && year != "Year") {
                        noResults.setVisibility(View.GONE);
                        generateReport.setEnabled(true);
                        generateReport.setBackgroundResource(R.drawable.signin_button_design);
                        getReport();
                    } else {
                        mHistoryAdapter.clear();
                        resultsHistory.clear();
                        generateReport.setEnabled(false);
                        generateReport.setBackgroundResource(R.drawable.gray_out_design);
                        noResults.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPdf();
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Success")
                        .setContentText("Generated report")
                        .setConfirmText("OK")
                        .setConfirmClickListener(null)
                        .show();
            }
        });


    }

    /*private void getReportFirst() {
        mHistoryAdapter.clear();
        resultsHistory.clear();
        itemList = 0;

        earnings = new ArrayList<>();
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
                        FetchRideInfoTwo(d.getKey());
                    }

                }
                else{
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInfoTwo(final String key) {

        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
                        try {
                            int amt = (int) dataSnapshot.child("amt_paid").getValue();
                            Double driverSalary = (amt * .20);
                            earning = (amt - driverSalary);
                            earnings.add(String.valueOf(earning));
                            Log.e("ddd",""+amt+", "+earning+", "+driverSalary);
                        } catch (Exception e) {
                        }
                        String finalDate = dataSnapshot.child("date").getValue().toString();
                        rideDate.add(finalDate);
                        AdminReportObject obj = new AdminReportObject(rideId, amt_paid, salary, finalDate, ""+earning);
                        resultsHistory.add(obj);

                        mHistoryAdapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

    private void createPdf() {
        earningFinals = 0.0;
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

            p = new Paragraph(new Phrase(20f, "Date Report: "+month+" Year: "+year, FontFactory.getFont(FontFactory.COURIER, 12f)));
            p.setAlignment(Element.ALIGN_CENTER);

            doc.add(p);

            final PdfPTable table = new PdfPTable(6);

            table.setPaddingTop(10);
            PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(25f, "Varescon Monthly Salary", FontFactory.getFont(FontFactory.COURIER, 18f))));
            cell.setPadding(10);
            cell.setColspan(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Date", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Ride ID", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(15f, "Customer Amount Paid", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Driver", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Driver Salary", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Earnings", FontFactory.getFont(FontFactory.COURIER_BOLD, 15f))));
            cell.setPadding(10);
            table.addCell(cell);


            for (int i = 0; i < itemList; i++) {
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(25f,"" + rideDate.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    table.addCell(cell);
                } catch (Exception e) {
                    table.addCell(new Paragraph(new Phrase(15f, "NULL", FontFactory.getFont(FontFactory.COURIER, 15f))));
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(25f, "" + rideID.get(i))));
                    cell.setPadding(10);
                    table.addCell(cell);
                }catch (Exception e){
                    table.addCell(new Paragraph(new Phrase(15f, "NULL", FontFactory.getFont(FontFactory.COURIER, 15f))));
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P " + amtPaid.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);

                }catch (Exception e){
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P 0", FontFactory.getFont(FontFactory.COURIER, 15f))));
                    cell.setPadding(10);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                }
                try {
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "" + driverName.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
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
                    cell = new PdfPCell(new Paragraph(new Phrase(15f, "P " + earnings.get(i), FontFactory.getFont(FontFactory.COURIER, 15f))));
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
                    Double earning = Double.parseDouble(earnings.get(i));
                    Log.e("salary", String.valueOf(earning));
                    earningFinals = (earningFinals + earning);
                } catch (Exception e) {
                }
            }



            table.setPaddingTop(10);
            cell = new PdfPCell(new Paragraph(new Phrase(25f, "Total Yearly Earnings: P "+String.format("%.2f", earningFinals), FontFactory.getFont(FontFactory.COURIER, 15f))));
            cell.setPadding(10);
            cell.setColspan(6);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);

            table.setPaddingTop(10);
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

        earnings = new ArrayList<>();
        rideID = new ArrayList<>();
        amtPaid = new ArrayList<>();
        driverSal = new ArrayList<>();
        rideDate = new ArrayList<>();
        salaryDriver = new ArrayList<>();
        driverName = new ArrayList<>();
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        FetchRideInfo(d.getKey());
                    }

                }
                else{
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void FetchRideInfo(final String key) {
        noResults.setVisibility(View.GONE);
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.history).child(key);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String[] date = dataSnapshot.child("date").getValue().toString().split(" ");
                    String monthD = date[0];
                    String yearD = date[2];
                    if (monthD.equals(month) && yearD.equals(year)) {
                        Log.e("asd",""+dataSnapshot.child("amt_paid").getValue());
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
                        try {
                            String amt = dataSnapshot.child("amt_paid").getValue().toString();
                            String driverSalary = dataSnapshot.child("driver_ride_salary").getValue().toString();
                            earning = ((Double.parseDouble(amt))-(Double.parseDouble(driverSalary)));
                            Log.e("earning",""+earning);
                            earnings.add(String.valueOf(earning));
                        } catch (Exception e) {
                            try {
                                String[] amt = dataSnapshot.child("amt_paid").getValue().toString().split(" ");
                                String driverSalary = dataSnapshot.child("driver_ride_salary").getValue().toString();
                                earning = ((Double.parseDouble(amt[1])) - (Double.parseDouble(driverSalary)));
                                Log.e("earning", "" + earning);
                                earnings.add(String.valueOf(earning));
                            }catch (Exception epo){}
                        }

                        String finalDate = dataSnapshot.child("date").getValue().toString();
                        rideDate.add(finalDate);
                        AdminReportObject obj = new AdminReportObject(rideId, amt_paid, salary, finalDate, String.valueOf(earning));
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
                    getDriverName(dataSnapshot.child("driver").getValue().toString());
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

    private void getDriverName(String driverId) {
        DatabaseReference userHistoryRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(driverId);
        userHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    driverName.add(""+dataSnapshot.child("firstname").getValue().toString()+" "+dataSnapshot.child("lastname").getValue());
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
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    private ArrayList<AdminReportObject> getDataSetHistory() {
        return resultsHistory;
    }
}
