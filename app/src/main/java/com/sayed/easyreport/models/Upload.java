package com.sayed.easyreport.models;

public class Upload {

    private String mName;
    private String mImageUrl;
    private String phoneNumber;
    private String crimeDesc;
    private String witnessInfo;
    private Double lat;
    private Double lon;

    public Upload() {
    }

    public Upload(String name, String imageUrl, String phoneNumber, String crimeDesc,String witnessInfo,Double lat, Double lon) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        this.mName = name;
        this.mImageUrl = imageUrl;
        this.phoneNumber = phoneNumber;
        this.crimeDesc = crimeDesc;
        this.witnessInfo = witnessInfo;
        this.lat = lat;
        this.lon = lon;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCrimeDesc() {
        return crimeDesc;
    }

    public String getWitnessInfo() {
        return witnessInfo;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}
