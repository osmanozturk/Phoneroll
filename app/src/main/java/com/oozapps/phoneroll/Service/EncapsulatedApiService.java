package com.oozapps.phoneroll.Service;

import android.util.Log;

import com.oozapps.phoneroll.DTO.FaceResponse;
import com.oozapps.phoneroll.DTO.FaceUrlRequestBody;
import com.oozapps.phoneroll.DTO.IdentifyRequestBody;
import com.oozapps.phoneroll.DTO.IdentifyResponse;
import com.oozapps.phoneroll.DTO.PersonRequest;
import com.oozapps.phoneroll.DTO.PersonResponse;
import com.oozapps.phoneroll.MidClasses.Candidates;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import webapipackage.ApiGeneral;

import static android.content.ContentValues.TAG;

public class EncapsulatedApiService {
    Retrofit retrofit = null;

    public void getFaceId(final String photoUrl, String blobName, final ServiceCallback sCallback) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiGeneral.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        APIService faceApiService = retrofit.create(APIService.class);
        Call<List<FaceResponse>> call = faceApiService.detectFaceFromUrl(ApiGeneral.apiKey, new FaceUrlRequestBody(photoUrl));


        call.enqueue(new Callback<List<FaceResponse>>() {
            @Override
            public void onResponse(Call<List<FaceResponse>> call, Response<List<FaceResponse>> response) {
                if (response.code() == 200) {
                    if (response.body().size() > 1) {
                        Log.e(TAG, "onResponse: More than one Face present");

                    }
                    String faceId = response.body().get(0).getFaceid();
                    Log.d(TAG, "onResponse: Successful " + faceId);
                    sCallback.getFaceIdComplete(faceId, blobName);
                } else if (response.code() == 400) {
                    Log.e(TAG, "onResponse: Bad");
                }
            }

            @Override
            public void onFailure(Call<List<FaceResponse>> call, Throwable t) {
                Log.e(TAG, "onFailure: Call Failed", t);
            }
        });

    }

    public void identifyWithFaceId(final String faceId, final ServiceCallback sCallback) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiGeneral.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        APIService faceApiService = retrofit.create(APIService.class);
        Call<List<IdentifyResponse>> call = faceApiService.getCandidateFromFaceId(ApiGeneral.apiKey,
                new IdentifyRequestBody("celebrities", faceId));


        call.enqueue(new Callback<List<IdentifyResponse>>() {
            @Override
            public void onResponse(Call<List<IdentifyResponse>> call, Response<List<IdentifyResponse>> response) {
                if (response.code() == 400) {
                    try {
                        Log.e(TAG, "onResponse: Bad" + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 200) {
                    List<Candidates> candidates = response.body().get(0).getCandidates();
                    if (candidates.size() > 0) {
                        String personId = candidates.get(0).getPersonid();
                        Log.d(TAG, "onResponse: Successful " + personId);
                        sCallback.identifyComplete(personId);
                    } else {
                        Log.e(TAG, "onResponse: No match Found");
                        sCallback.identifyComplete(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<IdentifyResponse>> call, Throwable t) {
                Log.e(TAG, "onFailure: Call Failed", t);
            }
        });

    }

    public void getPersonWithPersonId(final String personGroupId, final String personId, final ServiceCallback sCallback) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiGeneral.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        APIService faceApiService = retrofit.create(APIService.class);

        Call<PersonResponse> call = faceApiService.getPersonFromPersonId(ApiGeneral.apiKey, personGroupId, personId);
        call.enqueue(new Callback<PersonResponse>() {
            @Override
            public void onResponse(Call<PersonResponse> call, Response<PersonResponse> response) {
                if (response.code() == 400) {
                    try {
                        Log.e(TAG, "onResponse: Bad" + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 200) {
                    PersonResponse responsedPerson = response.body();
                    String name = responsedPerson.getName();
                    String userData = responsedPerson.getUserdata();
                    Log.d(TAG, "onResponse: Successful " + name);
                    sCallback.getPersonComplete(name, userData);

                }
            }

            @Override
            public void onFailure(Call<PersonResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Get Person Call Failed", t);
            }
        });
    }

    public void createPerson(final String name, final String userData, final ServiceCallback sCallback) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiGeneral.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        APIService faceApiService = retrofit.create(APIService.class);

        Call<PersonResponse> call = faceApiService.createPerson(ApiGeneral.apiKey, "celebrities",
                new PersonRequest(name, userData));
        call.enqueue(new Callback<PersonResponse>() {
            @Override
            public void onResponse(Call<PersonResponse> call, Response<PersonResponse> response) {
                if (response.code() == 400) {
                    Log.e(TAG, "onResponse: Bad Create Person Request Fail");
                } else if (response.code() == 200) {
                    Log.d(TAG, "onResponse: Successful on CreatePerson");
                    if (response.body() != null) {
                        sCallback.createPersonComplete(response.body().getPersonid());
                    } else {
                        Log.e(TAG, "onResponse: Create Person Response Body Null");
                    }
                }
            }

            @Override
            public void onFailure(Call<PersonResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Request Failed Create Person", t);
            }
        });
    }
}
