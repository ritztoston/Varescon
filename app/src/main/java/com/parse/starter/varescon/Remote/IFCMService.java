package com.parse.starter.varescon.Remote;

import com.parse.starter.varescon.Model.FCMResponse;
import com.parse.starter.varescon.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by iSwear on 12/24/2017.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA4VjYgZY:APA91bGeUq6t-dPnuuXZc_aarX0xIOUZOBYHEaQfkkNAheWv-XIsaQlPjmJz1jaOESjZyFrFDAD1YZ9gnpMhXkohWDjXM_4HliK4lY4cRwxpqeTuVLYv8m0-ruivN0ktfIz-sDXTh5jl"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
