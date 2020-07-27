package com.oozapps.phoneroll.MidClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Candidates {
    @Expose
    @SerializedName("confidence")
    private double confidence;
    @Expose
    @SerializedName("personId")
    private String personid;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }
}
