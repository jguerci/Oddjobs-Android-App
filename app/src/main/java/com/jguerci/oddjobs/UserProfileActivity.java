package com.jguerci.oddjobs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class UserProfileActivity extends AppCompatActivity {

    // UI Widgets
    private Button bLogout, bMessages, bNotifications, bPayment, bSettings;
    private ImageButton ibUserProfile;
    private TextView tvName, tvCityState;
    private  RatingBar rbRating;

    // Server Request
    private static final String USER_DETAILS_URI = "http://oddjobs.netne.net/FetchUserDetails.php";

    // User Credentials Storage
    private SharedPreferences sharedPrefs;
    private static final String USER_PREFS = "UserPreferences";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        bLogout = (Button) findViewById(R.id.profile_bLogout);
        bMessages = (Button) findViewById(R.id.profile_bMessages);
        bNotifications = (Button) findViewById(R.id.profile_bNotifications);
        bPayment = (Button) findViewById(R.id.profile_bPayment);
        bSettings = (Button) findViewById(R.id.profile_bSettings);
        ibUserProfile = (ImageButton) findViewById(R.id.profile_ibUserProfile);
        tvName = (TextView) findViewById(R.id.profile_tvName);
        tvCityState = (TextView) findViewById(R.id.profile_tvCityState);
        rbRating = (RatingBar) findViewById(R.id.profile_rbRating);

        bLogout.setOnClickListener(profileOnClickListener);
        bMessages.setOnClickListener(profileOnClickListener);
        bNotifications.setOnClickListener(profileOnClickListener);
        bPayment.setOnClickListener(profileOnClickListener);
        bSettings.setOnClickListener(profileOnClickListener);

        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        username = sharedPrefs.getString("username", null);

        rbRating.setRating(5);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (username != null) {
            String postParams = "username=" + username;
            new FetchUserDetails().execute(USER_DETAILS_URI, postParams);
        }
    }

    protected View.OnClickListener profileOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.profile_bLogout: {
                    Intent logoutIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logoutIntent);
                    break;
                }
                case R.id.profile_bMessages: {
                    break;
                }
                case R.id.profile_bNotifications: {
                    break;
                }
                case R.id.profile_bPayment: {
                    break;
                }
                case R.id.profile_bSettings: {
                    break;
                }
            }
        }
    };

    protected class FetchUserDetails extends AsyncTask<String, String, String> {
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

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[1]);
                writer.flush();
                writer.close();
                os.close();

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
                    tvName.setText(jsonObject.getString("firstname") + " " + jsonObject.getString("lastname"));
                    tvCityState.setText(jsonObject.getString("city") + ", " + jsonObject.getString("state"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
