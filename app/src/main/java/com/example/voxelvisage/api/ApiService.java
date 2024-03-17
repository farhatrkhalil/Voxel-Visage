package com.example.voxelvisage.api;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/process_images")
    Call<List<FaceModel>> processImages(@Body RequestBody images);
}

