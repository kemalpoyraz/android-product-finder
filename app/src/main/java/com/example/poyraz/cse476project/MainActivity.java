package com.example.poyraz.cse476project;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.os.StrictMode;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.content.*;
import android.graphics.Color;
import android.widget.Toast;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import de.greenrobot.event.EventBus;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends FragmentActivity {

    FragmentManager fm = getSupportFragmentManager();
    Toolbar tb;
    Button search;
    private Product product;
    private String barcodeNo;
    private String barcodeFormat;
    private int barcode = 1;
    private double lat;
    private double lon;
    private double nearbyLat;
    private double nearbyLon;
    private LatLng locationSelected;
    private String placeName;
    private String vicinity;
    private List <HashMap <String,String>> places;
    private HashMap <String,String> markerPlaceInformation = new HashMap <String, String>();
    private int markerFlag = 0;
    private String webService01 = "http://www.searchupc.com/handlers/upcsearch.ashx?request_type=3&access_token=C6F8AAC7-B32A-4BBC-8164-554B8292A4F5&upc=";
    //private String webService02 = "http://api.upcdatabase.org/json/00c43504185df24445806b297c0ed887/";
    private String webServiceFlag;
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);

        tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle ("Barcode");
        search = (Button) findViewById(R.id.search);
        setUpMapIfNeeded();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (barcode == 0) {

                    try {
                        barcodeLookUp (barcodeNo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    new JSONParse (webServiceFlag).execute();
                }
            }

        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick (Marker arg0) {

                markerFlag = 1;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom (arg0.getPosition(), 16.0f));
                arg0.showInfoWindow();
                LatLng from = new LatLng(lat,lon);
                LatLng to = arg0.getPosition();
                locationSelected = arg0.getPosition();
                String url = getDirection (from, to);
                DownloadTask dt = new DownloadTask();
                dt.execute(url);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {

        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap () {

        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange (Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                LatLng curLoc = new LatLng (location.getLatitude(), location.getLongitude());
                map.clear();
                if (nearbyLat != 0 && nearbyLon != 0){
                    if (markerFlag == 1){
                        LatLng from = new LatLng(lat, lon);
                        String url = getDirection (from, locationSelected);
                        DownloadTask dt = new DownloadTask();
                        dt.execute(url);
                    }
                    for(int i = 0; i < places.size(); i++){

                        MarkerOptions markerOptions = new MarkerOptions();
                        HashMap<String, String> hmPlace = places.get(i);
                        nearbyLat = Double.parseDouble(hmPlace.get("lat"));
                        nearbyLon = Double.parseDouble(hmPlace.get("lng"));
                        placeName = hmPlace.get("place_name");
                        vicinity = hmPlace.get("vicinity");
                        LatLng latLng = new LatLng(nearbyLat, nearbyLon);
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        markerOptions.title(placeName + ": " + vicinity);
                        Marker m = map.addMarker(markerOptions);
                        markerPlaceInformation.put(m.getId(), hmPlace.get("reference"));

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nearbyLat,nearbyLon), 16.0f));
                    }
                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 16.0f));
                map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current Location"));
            }
        };
        map.setOnMyLocationChangeListener(myLocationChangeListener);
    }

    private void barcodeLookUp (String barcodeNo) throws IOException {

        String productName = "null";
        String price = "null";
        String pattern = "<a href=\"http://m.barkodoku.com/urun/(.*?)\">";
        //String pricePattern = "<br />Fiyat: (.*?) ?<br />";
        String url = "http://m.barkodoku.com/";
        url = url + barcodeNo;

        URL barcodeSide = new URL (url);
        URLConnection yc = barcodeSide.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
        String inputLine;

        while ((inputLine = in.readLine()) != null){

            Pattern patternProductName = Pattern.compile(pattern);
            Matcher matcherProductName = patternProductName.matcher(inputLine);
            Pattern patternPrice = Pattern.compile(price);
            Matcher matcherPrice = patternPrice.matcher(inputLine);

            if(matcherProductName.find()) {
                productName = matcherProductName.group(1);
            }

            if(matcherPrice.find()){
                price = matcherPrice.group(1);
            }
        }
        LocalProductFragment dFragment = new LocalProductFragment().newInstance(productName, price);
        dFragment.show(fm, "Dialog Fragment");

        in.close();
    }

    private String getDirection (LatLng origin, LatLng destination) {

        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+destination.latitude+","+destination.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";

            while(( line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            }catch(Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            ParserDirectionsTask pt = new ParserDirectionsTask();
            pt.execute(result);

        }
    }

    public void scanBarcode (View v) {

        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void scanQR (View v) {

        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final Activity activity, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {

        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {}
        });

        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {

                barcodeNo = intent.getStringExtra("SCAN_RESULT");
                barcodeFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
                webServiceFlag = "";
                webServiceFlag = webService01 + barcodeNo;

                if (barcodeNo.startsWith("8") || barcodeNo.startsWith("5")){
                    barcode = 0;
                }
                else {
                    barcode = 1;
                }

                Toast toast = Toast.makeText(this, "BarcodeNumber:" + barcodeNo + " BarcodeFormat:" + barcodeFormat, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {

        String url;

        public JSONParse(String url){
            this.url = url;
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute (JSONObject json) {

            try {
                product = new Product();
                JSONObject response = json.getJSONObject("0");
                String productName = response.getString("productname");
                Double price = response.getDouble("price");
                String currency = response.getString("currency");
                String imageURL = response.getString("imageurl");

                product.setProductName(productName);
                product.setProductPrice(price);
                product.setProductCurrency(currency);
                product.setImageURL(imageURL);

                ProductFragment dFragment = new ProductFragment().newInstance(imageURL, productName, price, currency);
                dFragment.show(fm, "Dialog Fragment");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class PlacesTask extends AsyncTask <String, Integer, String> {

        String data = null;

        @Override
        protected String doInBackground(String... url) {

            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result){

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserDirectionsTask extends AsyncTask <String, Integer, List <List <HashMap <String,String>>>> {

        @Override
        protected List <List <HashMap <String, String>>> doInBackground (String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute (List <List <HashMap <String, String>>> result) {
            ArrayList <LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for(int i = 0; i < result.size(); i++) {

                points = new ArrayList <LatLng>();
                lineOptions = new PolylineOptions();
                List <HashMap <String, String>> path = result.get(i);

                for(int j = 0; j < path.size(); j++) {

                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.GREEN);
            }

            map.addPolyline(lineOptions);
        }
    }

    private class ParserTask extends AsyncTask <String, Integer, List <HashMap <String,String>>> {

        JSONObject jObject;

        @Override
        protected List <HashMap <String,String>> doInBackground (String... jsonData) {

            List <HashMap <String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);
                places = placeJsonParser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return places;
        }

        @Override
        protected void onPostExecute (List <HashMap <String,String>> list) {

            map.clear();
            places = list;

            for (int i = 0; i < list.size(); i++) {

                MarkerOptions markerOptions = new MarkerOptions();
                HashMap <String, String> hmPlace = list.get(i);
                nearbyLat = Double.parseDouble(hmPlace.get("lat"));
                nearbyLon = Double.parseDouble(hmPlace.get("lng"));
                placeName = hmPlace.get("place_name");
                vicinity = hmPlace.get("vicinity");
                LatLng latLng = new LatLng(nearbyLat, nearbyLon);
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerOptions.title(placeName + ": " + vicinity);
                Marker m = map.addMarker(markerOptions);
                markerPlaceInformation.put(m.getId(), hmPlace.get("reference"));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nearbyLat,nearbyLon), 16.0f));
            }
        }
    }
}
