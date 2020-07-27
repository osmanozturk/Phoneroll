package com.oozapps.phoneroll.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oozapps.phoneroll.MidClasses.Candidates;

import java.util.List;

public class IdentifyResponse {

    @Expose
    @SerializedName("candidates")
    private List<Candidates> candidates;
    @Expose
    @SerializedName("faceId")
    private String faceid;

    public List<Candidates> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidates> candidates) {
        this.candidates = candidates;
    }

    public String getFaceid() {
        return faceid;
    }

    public void setFaceid(String faceid) {
        this.faceid = faceid;
    }
}
