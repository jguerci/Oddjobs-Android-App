package com.jguerci.oddjobs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class PostAJob extends AppCompatActivity {

    // UI Widgets
    private EditText etTitle, etAddress, etCity, etState, etZIP;
    private EditText etCompletionTime, etPayment, etDescription;
    private Button bDate, bTime, bPostJob;
    private Spinner sCategories;

    // Server Request
    private ProgressDialog postJobDialog;
    private static final String POST_JOB_URI = "http://oddjobs.netne.net/PostJob.php";

    // User Credentials Storage
    private SharedPreferences sharedPrefs;
    private static final String USER_PREFS = "UserPreferences";
    private String username;

    private String jobTime = "";
    private String jobDate = "";
    private String category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ajob);

        etTitle = (EditText) findViewById(R.id.postajob_etJobTitle);
        etAddress = (EditText) findViewById(R.id.postajob_etAddress);
        etCity = (EditText) findViewById(R.id.postajob_etCity);
        etState = (EditText) findViewById(R.id.postajob_etState);
        etZIP = (EditText) findViewById(R.id.postajob_etZip);
        etCompletionTime = (EditText) findViewById(R.id.postajob_etCompletionTime);
        etPayment = (EditText) findViewById(R.id.postajob_etPayment);
        etDescription = (EditText) findViewById(R.id.postajob_etDescription);
        bDate = (Button) findViewById(R.id.postajob_bDate);
        bTime = (Button) findViewById(R.id.postajob_bTime);
        bPostJob = (Button) findViewById(R.id.postajob_bPostJob);
        sCategories = (Spinner) findViewById(R.id.postajob_sCategories);

        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        username = sharedPrefs.getString("username", null);

        bPostJob.setOnClickListener(postAJobListener);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategories.setPrompt("Categories");
        sCategories.setAdapter(adapter);
        sCategories.setOnItemSelectedListener(categoriesListener);
    }

    protected View.OnClickListener postAJobListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.postajob_bPostJob: {
                    if (etTitle.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A Title", Toast.LENGTH_SHORT).show();
                    } else if (etAddress.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter An Address", Toast.LENGTH_SHORT).show();;
                    } else if (etCity.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A City", Toast.LENGTH_SHORT).show();
                    } else if (etState.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A State", Toast.LENGTH_SHORT).show();
                    } else if (etZIP.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A ZIP Code", Toast.LENGTH_SHORT).show();
                    } else if (jobDate.equals("")) {
                        Toast.makeText(getApplicationContext(), "Pick A Date", Toast.LENGTH_SHORT).show();
                    } else if (jobTime.equals("")) {
                        Toast.makeText(getApplicationContext(), "Pick A Time", Toast.LENGTH_SHORT).show();
                    } else if (category.equals("") || category.equals("[Select a Category...]")) {
                        Toast.makeText(getApplicationContext(), "Select A Category", Toast.LENGTH_SHORT).show();
                    } else if (etCompletionTime.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A Completion Time", Toast.LENGTH_SHORT).show();
                    } else if (etPayment.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A Payment", Toast.LENGTH_SHORT).show();
                    } else if (etDescription.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Enter A Description", Toast.LENGTH_SHORT).show();
                    } else {
                        StringBuffer postParams = new StringBuffer("");
                        postParams.append("employer="      + username);
                        postParams.append("&title="        + etTitle.getText().toString());
                        postParams.append("&address="      + etAddress.getText().toString());
                        postParams.append("&city="         + etCity.getText().toString());
                        postParams.append("&state="        + etState.getText().toString());
                        postParams.append("&zip="          + etZIP.getText().toString());
                        postParams.append("&date="         + jobDate);
                        postParams.append("&time="         + jobTime);
                        postParams.append("&duration="     + etCompletionTime.getText().toString());
                        postParams.append("&payment="      + etPayment.getText().toString());
                        postParams.append("&description="  + etDescription.getText().toString());
                        postParams.append("&tag="          + category);

                        new PostJob().execute(POST_JOB_URI, postParams.toString());
                    }
                    break;
                }
            }
        }
    };

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    AdapterView.OnItemSelectedListener categoriesListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            category = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            jobTime = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
        }
    }

    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            StringBuilder s1 = new StringBuilder()
                    .append(year).append("-")
                    .append(month<10?"0"+(month+1):month + 1).append("-")
                    .append(day<10?"0"+day:day).append("");
            jobDate = s1.toString();
        }
    }

    protected class PostJob extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postJobDialog = new ProgressDialog(PostAJob.this);
            postJobDialog.setCancelable(false);
            postJobDialog.setMessage("Posting Job...");
            postJobDialog.show();
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
                JSONObject jsonObject = new JSONObject(result);
                String output = (String) jsonObject.get("output");
                if (output.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Job Posted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), EmployerActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Error" + output, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            postJobDialog.dismiss();
        }
    }
}
