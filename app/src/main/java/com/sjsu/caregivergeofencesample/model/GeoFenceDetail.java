package com.sjsu.caregivergeofencesample.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 * Model class to hold GeoFence details.
 * Created by Savio on 4/16/2017.
 */

public class GeoFenceDetail implements Parcelable{

    private String id;
    private String firstName;
    private String lastName;
    private double lat;
    private double lng;
    private long radius;

    public static final Parcelable.Creator<GeoFenceDetail> CREATOR = new Creator<GeoFenceDetail>() {
        @Override
        public GeoFenceDetail createFromParcel(Parcel source) {
            return new GeoFenceDetail(source);
        }

        @Override
        public GeoFenceDetail[] newArray(int size) {
            return new GeoFenceDetail[size];
        }
    };

    public GeoFenceDetail() {}

    private GeoFenceDetail(String id, String firstName, String lastName,
                           double lat, double lng, long radius) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    private GeoFenceDetail(Parcel parcel){
        String[] strParams = new String[3];
        parcel.readStringArray(strParams);

        id = strParams[0];
        firstName = strParams[1];
        lastName = strParams[2];

        double[] locationParams = new double[2];

        lat = locationParams[0];
        lng = locationParams[1];

        parcel.readDoubleArray(locationParams);
        radius = parcel.readLong();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public static List<GeoFenceDetail> getListFromJson(JSONArray array){
        List<GeoFenceDetail> output = new ArrayList<>();
        JSONObject curr;
        for (int i = 0; i < array.length(); i++) {
            try {
                curr = array.getJSONObject(i);
                output.add(new GeoFenceDetail(curr.getString("id"),
                        curr.getString("firstName"), curr.getString("lastName"),
                        curr.getDouble("lat"), curr.getDouble("lng"), curr.getLong("radius")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    public JSONObject getJson(){
        JSONObject json = new JSONObject();

        try {
            json.put("firstName", firstName);
            json.put("lastName", lastName);
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("radius", radius);
            json.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.id, this.firstName, this.lastName});
        dest.writeDoubleArray(new double[]{this.lat, this.lng});
        dest.writeLong(this.radius);
    }
}
