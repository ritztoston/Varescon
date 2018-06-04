package com.parse.starter.varescon.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by iSwear on 12/15/2017.
 */

public class GoogleMapAPI {
    private static Retrofit retrofit = null;

    public static  Retrofit getClient(String baseURL)
    {
        if(retrofit==null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
