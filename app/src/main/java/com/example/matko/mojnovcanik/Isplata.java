package com.example.matko.mojnovcanik;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
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
import java.util.ArrayList;

//Klasa za ekran Isplata koji sadrži formu za isplatu iznosa iz novčanika
public class Isplata extends ActionBarActivity {


    String imeIsplate;
    String iznosIsplate;


    EditText unosImeIsplata;
    EditText unosIznosIsplata;
    Button gumbIsplati;


    InputStream is=null;
    String result=null;
    String line=null;
    int code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_isplata);

        //pamti username kroz sve prozore
        Intent intent = getIntent();
        String korisnik = intent.getExtras().getString("korisnikDisplay");

        unosImeIsplata=(EditText)findViewById(R.id.imeIsplate);
        unosIznosIsplata=(EditText)findViewById(R.id.iznosIsplate);
        gumbIsplati = (Button)findViewById(R.id.gumbIsplati);

    }
//Metoda koja se poziva kada se pritisne gumb Isplati
    public void GumbIsplati(View v){
        imeIsplate = unosImeIsplata.getText().toString();

        iznosIsplate = unosIznosIsplata.getText().toString();

        //Provjerava se internet konekcija, ako internet nije uključen, iskače poruka
        if(isNetworkAvailable() == false)
        {
            Toast.makeText(Isplata.this, R.string.nemaInterneta, Toast.LENGTH_LONG).show();

        }
        //Sva polja moraju biti popunjena
        else if(imeIsplate.equals("") || iznosIsplate.equals("")) {
            Toast.makeText(Isplata.this, "Ispunite podatke do kraja", Toast.LENGTH_LONG).show();
        }


        else {

            //ako je sve u redu, poziva se metoda Isplati koja isplaćuje željeni iznos iz novčanika
            Isplati();
        }

    }

    //Metoda za isplaćivanje
    public void Isplati() {


        Intent intent = getIntent();
        String korisnik = intent.getExtras().getString("korisnikDisplay");


        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("imeIsplate", imeIsplate));
        nameValuePairs.add(new BasicNameValuePair("iznosIsplate", iznosIsplate));
        nameValuePairs.add(new BasicNameValuePair("ime", korisnik));

        try {
            //Povezuje se na server gdje se nalazi skripta koja se spaja na Mysql bazu podatka i isplaćuje željeni iznos
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://matkohorvat.cloudapp.net/Android-SpajanjeBaze/isplata.php");
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

            //skripta vraća pomoću JSON-a rezultat odnosno ako je vrijednost 1 onda je iznos uspješno isplaćen, ako je 0, onda je došlo do pogreške
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("rezultat"));

            if (code == 1) {
                Toast.makeText(Isplata.this, R.string.uspjesnaIsplata, Toast.LENGTH_LONG).show();
                finish();



                Intent i= new Intent(Isplata.this, Iznos.class);
                i.putExtra("korisnikDisplay",korisnik);
                finish();
                startActivity(i);



            } else {
                Toast.makeText(Isplata.this, R.string.neuspjesnaIsplata, Toast.LENGTH_LONG).show();


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


}
