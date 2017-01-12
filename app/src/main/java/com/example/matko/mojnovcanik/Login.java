package com.example.matko.mojnovcanik;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.NetworkInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;





//klasa za početni Login ekran

public class Login extends Activity implements View.OnClickListener {

    Button prijava;
    EditText username;
    EditText pass;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        prijava = (Button) findViewById(R.id.gumbLogin);
        username = (EditText) findViewById(R.id.usernameLog);
        pass = (EditText) findViewById(R.id.lozinkaLog);




        prijava.setOnClickListener(this);
    }

//Metoda koja se poziva kada se pritisne gumb za Login
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.gumbLogin:

                //Provjerava internet konekciju
                if(isNetworkAvailable() == false)
                {
                    Toast.makeText(Login.this, R.string.nemaInterneta, Toast.LENGTH_LONG).show();

                }
                //Provjerava da li su podaci ispunjeni do kraja
                else if(username.getText().toString().equals("") || pass.getText().toString().equals("")) {
                    Toast.makeText(Login.this, "Ispunite podatke do kraja", Toast.LENGTH_LONG).show();
                }

                else {
                    //Ako je sve u redu poziva se metoda Prijava koja prijavljuje korisnika i omogućuje mu da dalje koristi aplikaciju
                    new Prijava().execute();
                    break;
                }

        }

    }




    class Prijava extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Login.this);
        InputStream is = null;
        String result = "";

        protected void onPreExecute() {
            //Dijalog koji prikazuje učitavanje, odnosno prijavljivanje
            progressDialog.setMessage("Prijavljivanje...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface arg0) {
                    Prijava.this.cancel(true);
                }
            });
        }

        @Override
        //Spajanje na skriptu koja se spaja na tablicu korisnika i izlistava ih
        protected Void doInBackground(String... params) {
            String url_select = "http://matkohorvat.cloudapp.net/Android-SpajanjeBaze/prijava.php";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url_select);

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(param));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();

                //čita sadržaj
                is = httpEntity.getContent();

            } catch (Exception e) {

                Log.e("log_tag", "Error in http connection " + e.toString());

            }
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();

            } catch (Exception e) {

                Log.e("log_tag", "Error converting result " + e.toString());
            }

            return null;

        }

        protected void onPostExecute(Void v) {


            try {



                JSONArray Jarray = new JSONArray(result);
                //Skripta je izlistala sve korisnike iz tablice korisnici
                //Ovdje prolazimo kroz sve njih pomoću for petlje
                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject Jasonobject = null;

                    Jasonobject = Jarray.getJSONObject(i);


                    String ime = Jasonobject.getString("ime");
                    String lozinka = Jasonobject.getString("lozinka");

                    //Ako je neki korisnik iz tablice ima isto korisničko ime i lozinku kao što su oni uneseni u polju,
                    //tada ih se prosljeđuje dalje u aplikaciju, odnosno pokreće se novi Activity
                    if ((username.getText().toString().equals(ime)) && (getMd5Hash(pass.getText().toString())).equals(lozinka) ) {


                        final String korisnik = username.getText().toString();


                        Intent qq = new Intent(Login.this,
                                Iznos.class);
                        qq.putExtra("korisnikDisplay",korisnik);
                        startActivity(qq);


                        break;

                    }

                }


                this.progressDialog.dismiss();

            } catch (Exception e) {

                Log.e("log_tag", "Error parsing data " + e.toString());
            }
        }
    }






// klikom na gumb Registracija poziva se novi ekran Registracija

    public void OdiNaReg(View v){
        Intent i = new Intent(Login.this,
                Registracija.class);
        startActivity(i);
    }


    //klikom na gumb Info poziva se dialog ekran sa Informacijama INFO

    public void OdiNaInfo(View v){
        Intent i = new Intent(Login.this,
                Info.class);
        startActivity(i);
    }


    // Provjera da li je omogucen neki od mreznih adaptera
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //metoda za kriptiranje, hash MD5
    public static String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
                md5 = "0" + md5;

            return md5;
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getLocalizedMessage());
            return null;
        }
    }



}

