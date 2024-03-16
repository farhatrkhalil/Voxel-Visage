package com.example.voxelvisage.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FaceModel {

    @SerializedName("originalImage")
    private String originalImage;

    @SerializedName("imageWithLandmarks")
    private String imageWithLandmarks;

    @SerializedName("landmarksCoordinates")
    private List<List<Integer>> landmarksCoordinates;

    @SerializedName("faceCoordinates")
    private List<Integer> faceCoordinates;

    public String getOriginalImage() {
        return originalImage;
    }

    public String getImageWithLandmarks() {
        return imageWithLandmarks;
    }

    public List<List<Integer>> getLandmarksCoordinates() {
        return landmarksCoordinates;
    }

    public List<Integer> getFaceCoordinates() {
        return faceCoordinates;
    }
}
