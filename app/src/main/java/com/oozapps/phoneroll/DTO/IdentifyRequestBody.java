package com.oozapps.phoneroll.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class IdentifyRequestBody {

    @Expose
    @SerializedName("maxNumOfCandidatesReturned")
    private int maxnumofcandidatesreturned;
    @Expose
    @SerializedName("faceIds")
    private List<String> faceids;
    @Expose
    @SerializedName("personGroupId")
    private String persongroupid;

    public IdentifyRequestBody(String persongroupid, String faceId) {
        this.maxnumofcandidatesreturned = 1;
        this.persongroupid = persongroupid;
        List singleFaceList = new ArrayList<String>();
        singleFaceList.add(faceId);
        this.faceids = singleFaceList;
    }

    public int getMaxnumofcandidatesreturned() {
        return maxnumofcandidatesreturned;
    }

    public void setMaxnumofcandidatesreturned(int maxnumofcandidatesreturned) {
        this.maxnumofcandidatesreturned = maxnumofcandidatesreturned;
    }

    public List<String> getFaceids() {
        return faceids;
    }

    public void setFaceids(List<String> faceids) {
        this.faceids = faceids;
    }

    public String getpersongroupid() {
        return persongroupid;
    }

    public void setpersongroupid(String persongroupid) {
        this.persongroupid = persongroupid;
    }
}
