package com.oozapps.phoneroll.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PersonResponse {

    @Expose
    @SerializedName("userData")
    private String userdata;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("persistedFaceIds")
    private List<String> persistedfaceids;
    @Expose
    @SerializedName("personId")
    private String personid;

    public String getUserdata() {
        return userdata;
    }

    public void setUserdata(String userdata) {
        this.userdata = userdata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPersistedfaceids() {
        return persistedfaceids;
    }

    public void setPersistedfaceids(List<String> persistedfaceids) {
        this.persistedfaceids = persistedfaceids;
    }

    public String getPersonid() {
        return personid;
    }

    public void setPersonid(String personid) {
        this.personid = personid;
    }
}
