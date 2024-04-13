package com.voxelvisage.modelviewer;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("upload_images")
    Call<YourResponseModel> uploadImages(@Part List<MultipartBody.Part> images);
}
