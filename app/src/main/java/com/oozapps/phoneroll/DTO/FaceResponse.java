package com.oozapps.phoneroll.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oozapps.phoneroll.MidClasses.FaceRectangle;

public class FaceResponse {
    @Expose
    @SerializedName("faceRectangle")
    private FaceRectangle facerectangle;
    @Expose
    @SerializedName("faceId")
    private String faceid;
    public FaceResponse() {
        this.faceid = null;
        this.facerectangle = null;
    }

    public FaceRectangle getFacerectangle() {
        return facerectangle;
    }

    public void setFacerectangle(FaceRectangle facerectangle) {
        this.facerectangle = facerectangle;
    }

    public String getFaceid() {
        return faceid;
    }

    public void setFaceid(String faceid) {
        this.faceid = faceid;
    }
}
