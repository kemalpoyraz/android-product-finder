package com.example.poyraz.cse476project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceJSONParser {

    public List <HashMap <String, String>> parse (JSONObject jObject) {

        JSONArray places = null;

        try {
            places = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(places);
    }

    private List <HashMap <String, String>> getPlaces (JSONArray places) {

        int placesCount = places.length();
        List <HashMap<String, String>> placesList = new ArrayList <HashMap <String, String>>();
        HashMap <String, String> place = null;

        for(int i = 0; i < placesCount; i++) {

            try {

                place = getPlace((JSONObject)places.get(i));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap <String, String> getPlace (JSONObject jPlace) {

        HashMap <String, String> place = new HashMap <String, String>();
        String placeName = "-NA-";
        String vicinity="-NA-";
        String latitude="";
        String longitude="";
        String reference="";

        try {

            if(!jPlace.isNull("name")){
                placeName = jPlace.getString("name");
            }

            if(!jPlace.isNull("vicinity")){
                vicinity = jPlace.getString("vicinity");
            }

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = jPlace.getString("reference");

            place.put("placeName", placeName);
            place.put("vicinity", vicinity);
            place.put("latitude", latitude);
            place.put("longitude", longitude);
            place.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

}
