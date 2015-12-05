package com.jguerci.oddjobs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Server Request
    private static final String JOB_LOCATIONS_URI = "http://oddjobs.netne.net/FetchJobLocations.php";

    // User Credentials Storage
    private static final String USER_PREFS = "UserPreferences";

    private GoogleMap map;

    private IconGenerator iconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        iconFactory = new IconGenerator(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(markerClickListener);

        new FetchJobLocations().execute(JOB_LOCATIONS_URI);
    }

    protected GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            SharedPreferences.Editor editor = getSharedPreferences(USER_PREFS, MODE_PRIVATE).edit();
            editor.putString("jobID", marker.getSnippet());
            editor.commit();

            startActivity(new Intent(getApplicationContext(), ApplyForJob.class));
            return false;
        }
    };

    private void setMarker(String id, String tag, String location) {
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(location , 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList != null && addressList.size() != 0) {
            Address address = addressList.get(0);
            LatLng position = new LatLng(address.getLatitude() , address.getLongitude());

            iconFactory.setColor(Color.CYAN);

            // Store job_id in snippet for SQL queries
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(tag)))
                    .position(position)
                    .snippet(id)
                    .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

            map.addMarker(markerOptions);
        }
    }

    protected class FetchJobLocations extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            StringBuffer response = new StringBuffer("");

            try {
                URL url = new URL(params[0]);
                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(15000);
                con.setConnectTimeout(15000);
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);

                int code = con.getResponseCode();
                if (code == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("openjob_id");
                    String tag = jsonObject.getString("tag");
                    String address = jsonObject.getString("address");
                    setMarker(id, tag, address);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
