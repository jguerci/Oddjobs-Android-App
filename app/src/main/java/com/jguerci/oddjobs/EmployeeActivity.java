package com.jguerci.oddjobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class EmployeeActivity extends AppCompatActivity {

    // UI Widgets
    private TextView tvUsername, tvUpcomingJobs;
    private Button bHire, bSearchByLocation, bSearchByTime;
    private ImageButton ibUserProfile;
    private ListView lvUpcomingJobs;

    // Server Request
    private ProgressDialog fetchDialog;
    private static final String UPCOMING_JOBS_URI = "http://oddjobs.netne.net/FetchUpcomingJobs.php";

    // User Credentials Storage
    private SharedPreferences sharedPrefs;
    private static final String USER_PREFS = "UserPreferences";
    private String username;

    // Upcoming Jobs
    private ArrayList<String> upcomingJobs;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        tvUsername = (TextView) findViewById(R.id.employee_tvUsername);
        tvUpcomingJobs = (TextView) findViewById(R.id.employee_tvUpcomingJobs);
        bHire = (Button) findViewById(R.id.employee_bHire);
        bSearchByLocation = (Button) findViewById(R.id.employee_bSearchByLocation);
        bSearchByTime = (Button) findViewById(R.id.employee_bSearchByTime);
        ibUserProfile = (ImageButton) findViewById(R.id.employee_ibUserProfile);
        lvUpcomingJobs = (ListView) findViewById(R.id.employee_lvUpcomingJobs);

        ibUserProfile.setOnClickListener(employeeListener);
        bSearchByLocation.setOnClickListener(employeeListener);
        bSearchByTime.setOnClickListener(employeeListener);
        bHire.setOnClickListener(employeeListener);

        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        username = sharedPrefs.getString("username", null);

        tvUsername.setText(username);

        upcomingJobs = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(EmployeeActivity.this, android.R.layout.simple_list_item_1, upcomingJobs);
        lvUpcomingJobs.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        upcomingJobs.clear();
        String postParam = "employee=" + username;
        new FetchData().execute(UPCOMING_JOBS_URI, postParam);
    }

    protected View.OnClickListener employeeListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.employee_ibUserProfile: {
                    startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                    break;
                }
                case R.id.employee_bHire: {
                    startActivity(new Intent(getApplicationContext(), EmployerActivity.class));
                    break;
                }
                case R.id.employee_bSearchByLocation: {
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    break;
                }
                case R.id.employee_bSearchByTime: {
                    Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };

    protected class FetchData extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                fetchDialog = new ProgressDialog(EmployeeActivity.this);
                fetchDialog.setCancelable(false);
                fetchDialog.setMessage("Loading...");
                fetchDialog.show();
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
                    StringBuffer sBuffer = new StringBuffer("");
                    sBuffer.append(jsonObject.getString("date") + ": ");
                    sBuffer.append(jsonObject.getString("tag"));
                    upcomingJobs.add(sBuffer.toString());
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (fetchDialog.isShowing()) {
                fetchDialog.dismiss();
            }
        }
    }
}