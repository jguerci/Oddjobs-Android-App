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

public class EmployerActivity extends AppCompatActivity {

    // UI Widgets
    private TextView tvUsername, tvContractedJobs, tvUncontractedJobs;
    private Button bEarn, bRepostJob, bPostNewJob;
    private ImageButton ibUserProfile;
    private ListView lvContractedJobs, lvUncontractedJobs;

    // Server Request
    private ProgressDialog unconJobsDialog, conJobsDialog;
    private static final String CONTRACTED_JOBS_URI = "http://oddjobs.netne.net/FetchContractedJobs.php";
    private static final String UNCONTRACTED_JOBS_URI = "http://oddjobs.netne.net/FetchUncontractedJobs.php";

    // User Credentials Storage
    SharedPreferences settings;
    public static final String USER_PREFS = "UserPreferences";
    private String username;

    // Posted Jobs
    private ArrayList<String> conJobs, unconJobs;
    private ArrayAdapter<String> conAdapter, unconAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer);

        settings = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        username = settings.getString("username", null);

        tvUsername = (TextView) findViewById(R.id.employer_tvUsername);
        tvContractedJobs = (TextView) findViewById(R.id.employer_tvContractedJobs);
        tvUncontractedJobs = (TextView) findViewById(R.id.employer_tvUncontractedJobs);
        bEarn = (Button) findViewById(R.id.employer_bEarn);
        bRepostJob = (Button) findViewById(R.id.employer_bRepostJob);
        bPostNewJob = (Button) findViewById(R.id.employer_bPostNewJob);
        ibUserProfile = (ImageButton) findViewById(R.id.employer_ibUserProfile);
        lvContractedJobs = (ListView) findViewById(R.id.employer_lvContractedJobs);
        lvUncontractedJobs = (ListView) findViewById(R.id.employer_lvUncontractedJobs);

        ibUserProfile.setOnClickListener(employerListener);
        bRepostJob.setOnClickListener(employerListener);
        bPostNewJob.setOnClickListener(employerListener);
        bEarn.setOnClickListener(employerListener);

        tvUsername.setText(username);

        conJobs = new ArrayList<String>();
        conAdapter = new ArrayAdapter<String>(EmployerActivity.this, android.R.layout.simple_list_item_1, conJobs);
        lvContractedJobs.setAdapter(conAdapter);

        unconJobs = new ArrayList<String>();
        unconAdapter = new ArrayAdapter<String>(EmployerActivity.this, android.R.layout.simple_list_item_1, unconJobs);
        lvUncontractedJobs.setAdapter(unconAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String postParam = "employer=" + username;

        conJobs.clear();
        unconJobs.clear();

        new FetchContractedJobs().execute(CONTRACTED_JOBS_URI, postParam);
        new FetchUncontractedJobs().execute(UNCONTRACTED_JOBS_URI, postParam);
    }

    protected View.OnClickListener employerListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.employer_ibUserProfile: {
                    startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                    break;
                }
                case R.id.employer_bEarn: {
                    startActivity(new Intent(getApplicationContext(), EmployeeActivity.class));
                    break;
                }
                case R.id.employer_bRepostJob: {
                    Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.employer_bPostNewJob: {
                    startActivity(new Intent(getApplicationContext(), PostAJob.class));
                    break;
                }
            }
        }
    };

    protected class FetchUncontractedJobs extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            unconJobsDialog = new ProgressDialog(EmployerActivity.this);
            unconJobsDialog.setCancelable(false);
            unconJobsDialog.setMessage("Loading...");
            unconJobsDialog.show();
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
                    unconJobs.add(sBuffer.toString());
                }
                unconAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (unconJobsDialog.isShowing()) {
                unconJobsDialog.dismiss();
            }
        }
    }

    protected class FetchContractedJobs extends AsyncTask<String, String, String> {
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
                    conJobs.add(sBuffer.toString());
                }
                conAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
