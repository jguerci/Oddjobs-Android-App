package com.jguerci.oddjobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    // Activity Request Code
    private static final int SELECT_IMAGE = 101;

    // UI Widgets
    TextInputLayout tilLastName, tilFirstName, tilUsername, tilPassword, tilRepassword;
    TextInputLayout tilAddress, tilCity, tilState, tilZIP;
    EditText etLastName, etFirstName, etUsername, etPassword, etRepassword;
    EditText etAddress, etCity, etState, etZIP;
    Button bCreateProfile, bAddCard;
    ImageButton ibUserProfile;

    // Server Request URI
    private static final String URI = "http://cstrat67.comli.com/Register.php";

    // Server Request
    private StringRequest request;
    ProgressDialog registerDialog;
    ProgressDialog imageDialog;

    // User Profile Image
    private Uri fileURI = null;
    private String base64Image = "";
    private Bitmap bitmap;

    // User Credentials Storage
    SharedPreferences settings;
    public static final String USER_PREFS = "UserPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etLastName = (EditText) findViewById(R.id.register_etLastName);
        etFirstName = (EditText) findViewById(R.id.register_etFirstName);
        etUsername = (EditText) findViewById(R.id.register_etUsername);
        etPassword = (EditText) findViewById(R.id.register_etPassword);
        etRepassword = (EditText) findViewById(R.id.register_etRepassword);
        etAddress = (EditText) findViewById(R.id.register_etAddress);
        etCity = (EditText) findViewById(R.id.register_etCity);
        etState = (EditText) findViewById(R.id.register_etState);
        etZIP = (EditText) findViewById(R.id.register_etZIP);
        bAddCard = (Button) findViewById(R.id.register_bAddCard);
        bCreateProfile = (Button) findViewById(R.id.register_bCreateProfile);
        ibUserProfile = (ImageButton) findViewById(R.id.register_ibUserProfile);

        bAddCard.setOnClickListener(registerListener);
        bCreateProfile.setOnClickListener(registerListener);
        ibUserProfile.setOnClickListener(registerListener);
    }

    protected View.OnClickListener registerListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.register_bCreateProfile: {
                    request = new StringRequest(Request.Method.POST, URI, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String output = (String) jsonObject.get("output");
                                if (output.equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), EmployeeActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error: " + output, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            registerDialog.dismiss();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("lastname",  etLastName.getText().toString());
                            hashMap.put("firstname", etFirstName.getText().toString());
                            hashMap.put("username",  etUsername.getText().toString());
                            hashMap.put("password",  etPassword.getText().toString());
                            hashMap.put("address",   etAddress.getText().toString());
                            hashMap.put("city",      etCity.getText().toString());
                            hashMap.put("state",     etState.getText().toString());
                            hashMap.put("zip",       etZIP.getText().toString());
                            hashMap.put("image", base64Image);

                            return hashMap;
                        }
                    };
                    // Extend default socket timeout to account for large file uploads
                    RetryPolicy policy = new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    request.setRetryPolicy(policy);
                    RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(request);

                    registerDialog = new ProgressDialog(Register.this);
                    registerDialog.setCancelable(false);
                    registerDialog.setMessage("Creating Profile...");
                    registerDialog.show();
                    break;
                }
                case R.id.register_bAddCard: {
                    Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.register_ibUserProfile: {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    startActivityForResult(Intent.createChooser(getIntent, "Select Picture"), SELECT_IMAGE);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            fileURI = data.getData();
            //imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileURI);
                int bitmapWidth = (int) getResources().getDimension(R.dimen.bitmapWidth);
                int bitmapHeight = (int) getResources().getDimension(R.dimen.bitmapHeight);
                ibUserProfile.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new ImageEncoder().execute();
        }
    }

    /* Converts bitmap to base64 in worker thread to store as blob in MySQL database */
    private class ImageEncoder extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            imageDialog = new ProgressDialog(Register.this);
            imageDialog.setCancelable(false);
            imageDialog.setMessage("Processing Image...");
            imageDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            byte[] array = stream.toByteArray();
            base64Image = Base64.encodeToString(array, Base64.DEFAULT);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (imageDialog.isShowing()) {
                imageDialog.dismiss();
            }
        }
    }
}
