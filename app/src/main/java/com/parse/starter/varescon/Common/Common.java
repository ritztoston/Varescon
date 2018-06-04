package com.parse.starter.varescon.Common;

import android.location.Location;

import com.parse.starter.varescon.Model.Drivers;
import com.parse.starter.varescon.Model.User;
import com.parse.starter.varescon.Remote.FCMClient;
import com.parse.starter.varescon.Remote.GoogleMapAPI;
import com.parse.starter.varescon.Remote.IFCMService;
import com.parse.starter.varescon.Remote.IGoogleAPI;
import com.parse.starter.varescon.Remote.RetrofitClient;

/**
 * Created by iSwear on 12/3/2017.
 */

public class Common {

    public static final String availDrivers = "DriversAvailable";
    public static final String drivers = "Drivers";
    public static final String riders = "Riders";
    public static final String pickupRequest = "PickupRequest";
    public static final String tokens = "Tokens";
    public static final String historychild = "history";
    public static final String hailed = "hailed";
    public static final String history = "History";
    public static final String googleAPIUrl = "https://maps.googleapis.com/";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static double base_fare;
    public static double time_fare;
    public static double distance_fare;

    public static Location mLastLocation = null;
    public static User currentUser;
    public static Drivers currentDrivers;
    public static String currentGetuid;
    public static int driverRequest = 0, reserveTimeValue;
    public static double desLat, desLong, firstFare;
    public static String driverToken, riderToken;
    public static String distanceText;
    public static String timeText;
    public static String startAdd;
    public static String endAdd;
    public static double pickupLat, pickupLong;
    public static String userKey;
    public static String requestId;
    public static String startAddTwo;
    public static String endAddTwo;
    public static Integer reserveTimeValueTwo;
    public static Double firstFareTwo;
    public static String distanceTextTwo;
    public static String timeTextTwo;
    public static final String reserve_services = "Reserve";
    public static final String reserveChild = "reserve";
    public static String reserveId;
    public static String progress;
    public static String reserveDuration;
    public static Double reserveDistance;
    public static final double reserveFee = 0.25;
    public static boolean firstOpened = false;
    public static String monthDriver;
    public static String yearDriver;
    public static Location mLastLocationWalkin;
    public static String admin;
    public static String plate_no;
    public static String getAdminEmail;


    public static double getPrice(double km, int min) {
        return (base_fare + (time_fare * min) + (distance_fare * km));
    }

    //MAPS
    public static final String baseURL = "https://maps.googleapis.com";

    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IGoogleAPI getGoogleService() {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static void resetVariables() {
        mLastLocation = null;
        currentUser = null;
        driverRequest = 0;
        desLat = 0;
        desLong = 0;
        driverToken = null;
        riderToken = null;
    }
}
