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

//Klasa za ekran Uplata koji sadrži formu za uplatu iznosa iz novčanika
public class Uplata extends ActionBarActivity {

    String imeUplate;
    String iznosUplate;


    EditText unosImeUplata;
    EditText unosIznosUplata;
    Button gumbUplati;


    InputStream is=null;
    String result=null;
    String line=null;
    int code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_uplata);

        //pamti username kroz sve prozore
        Intent intent = getIntent();
        String korisnik = intent.getExtras().getString("korisnikDisplay");

        unosImeUplata=(EditText)findViewById(R.id.imeUplate);
        unosIznosUplata=(EditText)findViewById(R.id.iznosUplate);
        gumbUplati = (Button)findViewById(R.id.gumbUplati);

    }

    //Metoda koja se poziva kada se pritisne gumb Uplati
public void GumbUplati(View v){
    imeUplate = unosImeUplata.getText().toString();
    //iznosUplate.parseDouble(unosIznosUplata.getText().toString());
    iznosUplate = unosIznosUplata.getText().toString();

    //Provjerava se internet konekcija, ako internet nije uključen, iskače poruka
    if(isNetworkAvailable() == false)
    {
        Toast.makeText(Uplata.this, R.string.nemaInterneta, Toast.LENGTH_LONG).show();

    }
    //Sva polja moraju biti popunjena
    else if(imeUplate.equals("") || iznosUplate.equals("")) {
        Toast.makeText(Uplata.this, "Ispunite podatke do kraja", Toast.LENGTH_LONG).show();
    }


    else {

        //ako je sve u redu, poziva se metoda Uplati koja uplaćuje željeni iznos u novčanika
        Uplati();
    }

}




    //Metoda za uplaćivanje
    public void Uplati() {


        Intent intent = getIntent();
        String korisnik = intent.getExtras().getString("korisnikDisplay");


        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("imeUplate", imeUplate));
        nameValuePairs.add(new BasicNameValuePair("iznosUplate", iznosUplate));
        nameValuePairs.add(new BasicNameValuePair("ime", korisnik));

        try {
            //Povezuje se na server gdje se nalazi skripta koja se spaja na Mysql bazu podatka i uplaćuje željeni iznos
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://matkohorvat.cloudapp.net/Android-SpajanjeBaze/uplata.php");
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
            //skripta vraća pomoću JSON-a rezultat odnosno ako je vrijednost 1 onda je iznos uspješno uplaćen, ako je 0, onda je došlo do pogreške
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("rezultat"));

            if (code == 1) {
                Toast.makeText(Uplata.this, R.string.uspjesnaUplata, Toast.LENGTH_LONG).show();
                finish();



                Intent i= new Intent(Uplata.this, Iznos.class);
                i.putExtra("korisnikDisplay",korisnik);
                finish();
                startActivity(i);



            } else {
                Toast.makeText(Uplata.this, R.string.neuspjesnaUplata, Toast.LENGTH_LONG).show();


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
