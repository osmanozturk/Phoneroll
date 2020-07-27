package com.oozapps.phoneroll.Service;

import com.oozapps.phoneroll.DTO.FaceResponse;
import com.oozapps.phoneroll.DTO.FaceUrlRequestBody;
import com.oozapps.phoneroll.DTO.IdentifyRequestBody;
import com.oozapps.phoneroll.DTO.IdentifyResponse;
import com.oozapps.phoneroll.DTO.PersonRequest;
import com.oozapps.phoneroll.DTO.PersonResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {
    @Headers({"Content-Type: application/json"})
    @POST("face/v1.0/detect")
    Call<List<FaceResponse>> detectFaceFromUrl(@Header("Ocp-Apim-Subscription-Key") String apiKey, @Body FaceUrlRequestBody url);

    @Headers({"Content-Type: application/json"})
    @POST("face/v1.0/identify")
    Call<List<IdentifyResponse>> getCandidateFromFaceId(@Header("Ocp-Apim-Subscription-Key") String apiKey, @Body IdentifyRequestBody face);

    @Headers({"Content-Type: application/json"})
    @POST("face/v1.0/persongroups/{personGroupId}/persons")
    Call<PersonResponse> createPerson(@Header("Ocp-Apim-Subscription-Key") String apiKey,
                                      @Path(value = "personGroupId", encoded = true) String personGroupId,
                                      @Body PersonRequest person);

    @Headers({"Content-Type: application/json"})
    @GET("face/v1.0/persongroups/{personGroupId}/persons/{personId}")
    Call<PersonResponse> getPersonFromPersonId(@Header("Ocp-Apim-Subscription-Key") String apiKey,
                                               @Path(value = "personGroupId", encoded = true) String personGroupId,
                                               @Path(value = "personId", encoded = true) String personId);


}
