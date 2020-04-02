package com.example.covid_19tracker;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

// CLASS CUSTOM MODEL UNTUK OBJECT DATA CORONA

public class Corona implements Parcelable {

    // deklarasi variabel yang ada di object corona

    private String country;
    private Integer confirmed;
    private Integer deaths;
    private Integer recovered;
    private String date;
    private float fatalityRate;
    private ArrayList<String> dateSeries;
    private ArrayList<Integer> confirmedSeries;
    private ArrayList<Integer> activeCaseSeries;

    public Corona(String country, JSONObject obj, ArrayList<String> dateSeries, ArrayList<Integer> confirmedSeries,
                  ArrayList<Integer> activeCaseSeries) {     // constructor yang akan menset variabel di object corona berdasarkan object JSON yang didapat dari URL API
        try {
            this.country = country;
            this.date = obj.getString("date");
            this.confirmed = obj.getInt("confirmed");
            this.deaths = obj.getInt("deaths");

            if (deaths == 0) {
                this.fatalityRate = 0;
            } else {
                this.fatalityRate = ( (float) deaths / confirmed ) * 100;
            }

            this.dateSeries = new ArrayList<>(dateSeries);
            this.confirmedSeries = new ArrayList<>(confirmedSeries);

            this.recovered = obj.getInt("recovered");

            this.activeCaseSeries = new ArrayList<>(activeCaseSeries);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // GETTERS DAN SETTERS
    public String getCountry() { return country; }

    public String getDate() { return date; }

    public Integer getConfirmed() { return confirmed; }

    public Integer getDeaths() { return deaths; }

    public Integer getRecovered() { return recovered; }

    public float getFatalityRate() { return fatalityRate; }

    public ArrayList<String> getDateSeries() { return dateSeries; }

    public ArrayList<Integer> getConfirmedSeries() { return confirmedSeries; }

    public ArrayList<Integer> getActiveCaseSeries() { return activeCaseSeries; }

    // membaca isi parcel
    protected Corona(Parcel in) {
        country = in.readString();
        date = in.readString();
        confirmed = in.readInt();
        deaths = in.readInt();
        fatalityRate = in.readFloat();
        recovered = in.readInt();

        dateSeries = in.readArrayList(null);
        confirmedSeries = in.readArrayList(null);
        activeCaseSeries = in.readArrayList(null);
    }

    public static final Creator<Corona> CREATOR = new Creator<Corona>() {
        @Override
        public Corona createFromParcel(Parcel in) {
            return new Corona(in);
        }

        @Override
        public Corona[] newArray(int size) {
            return new Corona[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // menulis ke parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(date);
        dest.writeInt(confirmed);
        dest.writeInt(deaths);
        dest.writeFloat(fatalityRate);
        if (recovered != null) {
            dest.writeInt(recovered);
        } else {
            dest.writeInt(0);
        }

        dest.writeList(dateSeries);
        dest.writeList(confirmedSeries);

        if (activeCaseSeries != null) {
            dest.writeList(activeCaseSeries);
        } else {
            ArrayList<Integer> emptyList = new ArrayList<>();
            emptyList.add(0);
            dest.writeList(emptyList);
        }

    }
}
