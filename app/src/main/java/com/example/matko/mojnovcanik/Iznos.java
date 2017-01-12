package com.example.matko.mojnovcanik;


import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
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


public class Iznos extends ActionBarActivity {

    //Potrebno je kako bi swipanje bilo moguće
    private GestureDetectorCompat gestureDetectorCompat;


    TextView korisnikPrikaz;

    TextView stanje;

    InputStream is=null;
    String result=null;
    String line=null;


    Double iznosFinal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_iznos);

       //potrebno za Swipanje
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());

        stanje = (TextView)findViewById(R.id.iznos);

        //Uzima string iz Login Activitija i tu ga prikazuje
        Intent intent = getIntent();
       String korisnik = intent.getExtras().getString("korisnikDisplay");

        korisnikPrikaz = (TextView)findViewById(R.id.korisnik);
        korisnikPrikaz.setText("Dobrodošli "+korisnik);

        //Poziva se metoda za prikazivanje stanja u novčaniku korisnika, odnosno ona uzima sumu iz tablica gdje se nalaze uplate korisnika te
        //sumu isplata koje se nalaze u tablici isplata te na kraju od uplata oduzme isplate te konačan rezultat zapiše u TextView

       prikazStanja();


    }

//METODA ZA ISPISIVANJE STANJA

    public void prikazStanja() {

        Intent intent = getIntent();
        String korisnik = intent.getExtras().getString("korisnikDisplay");

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("ime", korisnik));

        try {
            //spajanje na server gdje se nalazi skripta koja obavlja čitanja suma i konačnog preračunavanja i prikazivanja rezultata
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://matkohorvat.cloudapp.net/Android-SpajanjeBaze/prikazStanja.php");
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
                    (new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "konekcija uspješna ");
        } catch (Exception e) {
            Log.e("Fail 2", e.toString());
        }

        try {

            JSONObject json_data = new JSONObject(result);
            iznosFinal = (json_data.getDouble("iznos"));

            //Ispis konačnog rezultata
            stanje.setText(iznosFinal+" kn");






    } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }





    }



//METODA ZA ISPISIVANJE STANJA ---KRAJ




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_iznos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        //Poziva se obrazac za uplatu iz menija
        if (id == R.id.uplata) {

            Intent intent = getIntent();
            String korisnik = intent.getExtras().getString("korisnikDisplay");

            Intent i = new Intent(Iznos.this,
                    Uplata.class);
            i.putExtra("korisnikDisplay",korisnik);
            finish();
            startActivity(i);

        }

        //Poziva se obrazac za isplatu iz menija
        else if(id == R.id.isplata){

            Intent intent = getIntent();
            String korisnik = intent.getExtras().getString("korisnikDisplay");

            Intent i= new Intent(Iznos.this, Isplata.class);
            i.putExtra("korisnikDisplay",korisnik);
            finish();
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

//za swipanje POČETAK

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {


               //ako se swipea u LIJEVO
            if(event2.getX() < event1.getX()){

                Intent intent = getIntent();
                String korisnik = intent.getExtras().getString("korisnikDisplay");

                //prebacivanje na UPLATE
                Intent i = new Intent(Iznos.this,
                        PopisUplata.class);
                i.putExtra("korisnikDisplay",korisnik);
                finish();
                startActivity(i);
            }


            return true;
        }
    }
}

//KRAJ za swipanje



