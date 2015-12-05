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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {

    // Activity Request Code
    private static final int SELECT_IMAGE = 101;

    // UI Widgets
    TextInputLayout tilLastName, tilFirstName, tilUsername, tilPassword, tilRepassword;
    TextInputLayout tilAddress, tilCity, tilState, tilZIP;
    EditText etLastName, etFirstName, etUsername, etPassword, etRepassword;
    EditText etAddress, etCity, etState, etZIP;
    Button bCreateProfile, bAddCard;
    ImageButton ibUserProfile;

    // Server Request
    ProgressDialog imageDialog, registerDialog;
    private static final String REGISTER_URI = "http://oddjobs.netne.net/Register.php";

    // User Profile Image
    private Uri fileURI = null;
    private String base64Image = "";
    private Bitmap bitmap;

    // User Credentials Storage
    SharedPreferences sharedPrefs;
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

        sharedPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        sharedPrefs.edit().clear().commit();
    }

    protected View.OnClickListener registerListener = new View.OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.register_bCreateProfile: {
                    StringBuffer postParams = new StringBuffer("");
                    postParams.append("lastname="   + etLastName.getText().toString());
                    postParams.append("&firstname=" + etFirstName.getText().toString());
                    postParams.append("&username="  + etUsername.getText().toString());
                    postParams.append("&password="  + etPassword.getText().toString());
                    postParams.append("&address="   + etAddress.getText().toString());
                    postParams.append("&city="      + etCity.getText().toString());
                    postParams.append("&state="     + etState.getText().toString());
                    postParams.append("&zip="       + etZIP.getText().toString());
                    postParams.append("&image="     + base64Image);

                    new Register().execute(REGISTER_URI, postParams.toString());
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

    /* Converts bitmap to base64 for storage in SQL database */
    private class ImageEncoder extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            imageDialog = new ProgressDialog(RegisterActivity.this);
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

    protected class Register extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            registerDialog = new ProgressDialog(RegisterActivity.this);
            registerDialog.setCancelable(false);
            registerDialog.setMessage("Creating Profile...");
            registerDialog.show();
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
    }
}
