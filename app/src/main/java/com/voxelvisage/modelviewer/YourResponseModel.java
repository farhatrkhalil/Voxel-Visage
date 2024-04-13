package com.voxelvisage.modelviewer;

import com.google.gson.annotations.SerializedName;

public class YourResponseModel {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}