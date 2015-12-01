package com.jguerci.oddjobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    // UI Widgets
    TextInputLayout tilUsername, tilPassword;
    EditText etUsername, etPassword;
    TextView tvRegister;
    Button bLogin;

    // Server Request URI
    private static final String URI = "http://cstrat67.comli.com/Login.php";

    // Server Request
    private StringRequest request;
    ProgressDialog loginDialog;

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
    }

    protected View.OnClickListener loginListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.login_bLogin: {
                    request = new StringRequest(Request.Method.POST, URI, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String output = (String) jsonObject.get("output");
                                if (output.equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
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
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("username", etUsername.getText().toString());
                            hashMap.put("password", etPassword.getText().toString());

                            return hashMap;
                        }
                    };
                    RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(request);

                    loginDialog = new ProgressDialog(Login.this);
                    loginDialog.setCancelable(true);
                    loginDialog.setMessage("Logging in...");
                    loginDialog.show();
                    break;
                }
                case R.id.login_tvRegister: {
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    break;
                }
            }
        }
    };
}
