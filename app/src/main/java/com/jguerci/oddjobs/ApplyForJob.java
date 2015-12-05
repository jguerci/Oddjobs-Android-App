package com.jguerci.oddjobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import javax.net.ssl.HttpsURLConnection;

public class ApplyForJob extends AppCompatActivity {

    // UI Widgets
    TextView tvTitle, tvAddress, tvCityState, tvDateTime;
    TextView tvPayment, tvDuration, tvCategory, tvEmployer;
    ImageButton ibUserProfile;
    EditText etDescription;
    Button bContact, bApply;

    // Server Request
    private ProgressDialog fetchDialog;
    private static final String JOB_DETAILS_URI = "http://oddjobs.netne.net/FetchJobDetails.php";
    private static final String APPLY_FOR_JOB_URI = "http://oddjobs.netne.net/ApplyForJob.php";

    // User Credentials Storage
    private SharedPreferences sharedPrefs;
    private static final String USER_PREFS = "UserPreferences";
    private String username, openjob_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_job);

        tvTitle = (TextView) findViewById(R.id.apply_tvTitle);
        tvAddress = (TextView) findViewById(R.id.apply_tvAddress);
        tvCityState = (TextView) findViewById(R.id.apply_tvCityState);
        tvDateTime = (TextView) findViewById(R.id.apply_tvDateTime);
        tvPayment = (TextView) findViewById(R.id.apply_tvPayment);
        tvDuration = (TextView) findViewById(R.id.apply_tvDuration);
        tvCategory = (TextView) findViewById(R.id.apply_tvCategory);
        tvEmployer = (TextView) findViewById(R.id.apply_tvEmployer);
        ibUserProfile = (ImageButton) findViewById(R.id.apply_ibUserProfile);
        etDescription = (EditText) findViewById(R.id.apply_etDescription);
        bContact = (Button) findViewById(R.id.apply_bContact);
        bApply = (Button) findViewById(R.id.apply_bApply);

        bContact.setOnClickListener(employeeListener);
        bApply.setOnClickListener(employeeListener);

        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        openjob_id = prefs.getString("jobID", null);
        username = prefs.getString("username", null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (openjob_id != null) {
            String postParams = "openjob_id=" + openjob_id;
            new FetchJobDetails().execute(JOB_DETAILS_URI, postParams);
        }
    }

    protected View.OnClickListener employeeListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.apply_bContact: {
                    Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.apply_bApply: {
                    if (openjob_id != null && username != null) {
                        StringBuffer params = new StringBuffer("openjob_id=").append(openjob_id)
                                .append("&employee=").append(username);
                        new ApplyTask().execute(APPLY_FOR_JOB_URI, params.toString());
                    }
                    Toast.makeText(getApplicationContext(), "Job Accepted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), EmployeeActivity.class));
                    break;
                }
            }
        }
    };

    protected class FetchJobDetails extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fetchDialog = new ProgressDialog(ApplyForJob.this);
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
                    tvTitle.setText(jsonObject.getString("title"));
                    tvAddress.setText(jsonObject.getString("address"));
                    tvCityState.setText(jsonObject.getString("city") + ", " + jsonObject.getString("state"));
                    tvDateTime.setText(jsonObject.getString("date") + " " + jsonObject.getString("time"));
                    tvPayment.setText(jsonObject.getString("payment"));
                    tvDuration.setText(jsonObject.getString("duration"));
                    tvCategory.setText(jsonObject.getString("tag"));
                    tvEmployer.setText(jsonObject.getString("employer"));
                    etDescription.setText(jsonObject.getString("description"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (fetchDialog.isShowing()) {
                fetchDialog.dismiss();
            }
        }
    }

    protected class ApplyTask extends AsyncTask<String, String, String> {
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
        }
    }
}
