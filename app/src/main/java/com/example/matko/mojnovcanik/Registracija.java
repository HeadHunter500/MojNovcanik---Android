package com.example.matko.mojnovcanik;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

//klasa za ekran za registraciju

public class Registracija extends Activity {

    String ime;
    String pass1;
    String pass2;

    EditText unosIme;
    EditText unosPass1;
    EditText unosPass2;



    InputStream is=null;
    String result=null;
    String line=null;
    int code;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registracija);


         unosIme=(EditText) findViewById(R.id.usernameReg);
         unosPass1=(EditText) findViewById(R.id.lozinkaReg1);
         unosPass2=(EditText) findViewById(R.id.lozinkaReg2);
        Button gumbReg=(Button) findViewById(R.id.registracijaReg);



        gumbReg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                ime = unosIme.getText().toString();
                pass1 = unosPass1.getText().toString();
                pass2 = unosPass2.getText().toString();


                //Provjerava se da li je internet uključen
                if(isNetworkAvailable() == false)
                {
                    Toast.makeText(Registracija.this, R.string.nemaInterneta, Toast.LENGTH_LONG).show();

                }
                //Provjerava se da li su sva polja ispunjena
                else if(ime.equals("") || pass1.equals("") || pass2.equals("")) {
                    Toast.makeText(Registracija.this, "Ispunite podatke do kraja", Toast.LENGTH_LONG).show();
                }
                //Provjerava da li su oba polja za lozinek ispunjena istom lozinkom
                else if(!(pass1.equals(pass2))){
                    Toast.makeText(Registracija.this, "Lozinke moraju biti iste", Toast.LENGTH_LONG).show();
                    unosPass1.setText("");
                    unosPass2.setText("");
                }

                else {
                    //AKo je sve u redu tada se poziva metoda Registriraj koja pomoću skripte unosi korisnika u bazu podataka, odnosno ga registrira
                    Registriraj();
                }

               }

        });


    }


//Metoda za registraciju
    public void Registriraj() {



        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("ime", ime));
        nameValuePairs.add(new BasicNameValuePair("pass1", pass1));

        try {
            //Spajanje na server gdje se nalazi skripta koja unosi korisnika u bazu podataka
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://matkohorvat.cloudapp.net/Android-SpajanjeBaze/registracija.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "konekcija uspješna ");
        } catch (Exception e) {
            Log.e("Fail 1", e.toString());
            Toast.makeText(getApplicationContext(), "Pogrešna IP Adresa",
                    Toast.LENGTH_LONG).show();
        }

        try {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);

            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "konekcija uspješna ");
        } catch (Exception e) {
            Log.e("Fail 2", e.toString());
        }

        try {
            //Skripta je ubacila korisnika u bazu podataka
            //vraća se pomoću JSON-a rezultat 1 ako je sve prošlo ok ili 0 ako je registracija bila neuspješna
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("rezultat"));

            if (code == 1) {
                Toast.makeText(Registracija.this, R.string.uspjesnaReg, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(Registracija.this, R.string.usernamePostoji, Toast.LENGTH_LONG).show();


            }
        } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }
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
