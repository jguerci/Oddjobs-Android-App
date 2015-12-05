package com.jguerci.oddjobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    // UI Widgets
    private TextInputLayout tilUsername, tilPassword;
    private EditText etUsername, etPassword;
    private TextView tvRegister;
    private Button bLogin;

    // Server Request
    private ProgressDialog loginDialog;
    private static final String LOGIN_URI = "http://oddjobs.netne.net/Login.php";

    // User Credentials Storage
    private SharedPreferences sharedPrefs;
    private static final String USER_PREFS = "UserPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilUsername = (TextInputLayout) findViewById(R.id.login_tilUsername);
        tilPassword = (TextInputLayout) findViewById(R.id.login_tilPassword);
        etUsername = (EditText) findViewById(R.id.login_etUsername);
        etPassword = (EditText) findViewById(R.id.login_etPassword);
        bLogin = (Button) findViewById(R.id.login_bLogin);
        tvRegister = (TextView) findViewById(R.id.login_tvRegister);

        bLogin.setOnClickListener(loginListener);
        tvRegister.setOnClickListener(loginListener);

        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        sharedPrefs.edit().clear().commit();
    }

    protected View.OnClickListener loginListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.login_bLogin: {
                    StringBuffer postParams = new StringBuffer("");
                    postParams.append("username=" + etUsername.getText().toString());
                    postParams.append("&password=" + etPassword.getText().toString());

                    new Login().execute(LOGIN_URI, postParams.toString());

                    break;
                }
                case R.id.login_tvRegister: {
                    startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    break;
                }
            }
        }
    };

    protected class Login extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginDialog = new ProgressDialog(LoginActivity.this);
            loginDialog.setCancelable(true);
            loginDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loginDialog.setMessage("Logging in...");
            loginDialog.show();
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
                    sharedPrefs.edit().putString("username", etUsername.getText().toString()).commit();
                    startActivity(new Intent(getApplicationContext(), EmployeeActivity.class));
                }
                else {
                    Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loginDialog.dismiss();
        }
    }
}
