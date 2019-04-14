package com.example.v.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.v.bluetooth.QeryUtils.makeHttpRequest;

public class Main3Activity extends AppCompatActivity {
    TextView tvCurrentHeart;
    TextView tvCurrentGlucose;
    TextView tvLevel;
    Context context;
    String glucose;
    String heart;
    private String urlH = "http://192.168.43.166/healthCare4/showHeart.php";
    private String urlG = "http://192.168.43.166/healthCare4/showGlucose.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        context = this;
        Button btStartHeart = (Button)findViewById(R.id.btstartHeart);
        Button btStartGlucose = (Button)findViewById(R.id.btstartGlucose);
        Button btMail = (Button)findViewById(R.id.btMail);
        tvCurrentHeart = (TextView) findViewById(R.id.tvCurrentHeart);
        tvCurrentGlucose = (TextView) findViewById(R.id.tvCurrentGlucose);
        tvLevel = (TextView) findViewById(R.id.tvlevel);


        btStartHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        btStartGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(intent);
            }
        });
        btMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] address = {"u1304097@student.cuet.ac.bd"};
                String subject = "Health Care";
                String body = "\nGlucose : " + glucose + "\nHeart rate : " +heart;
                composeEmail(address, subject, body);
            }
        });
        new HeartAsync().execute(urlH);
        new GlucoseAsync().execute(urlG);

    }
    public void composeEmail(String[] addresses, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new HeartAsync().execute(urlH);
        new GlucoseAsync().execute(urlG);
    }
    class GlucoseAsync extends AsyncTask<String,Void,JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject json = makeHttpRequest(strings[0], null, null, null, null, context);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);
            if (jsonObject == null) {
                Toast.makeText(context, "CONNECTION ERROR", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONArray jsonArray = null;
                try {
                    jsonArray = jsonObject.getJSONArray("array");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        glucose = object.getString("glucose");
                    }
                    String s = glucose.trim();
                    tvCurrentGlucose.setText(glucose);
                    int[] a = new int[30];
                    int count = 0;
                    for (int i = 0; i < s.length(); i++){
                        char c = s.charAt(i);
                        if(c == '.')
                            break;
                        a[i] = (int)(c - '0');
                        count++;
                    }
                    int l = 0;
                    int b = 1;
                    while (count > 0){
                        l += a[--count] * b;
                        b *= 10;
                    }
                    String ll = null;
                    if (l >= 120 && l <= 220)
                        ll = "NORMAL";
                    else if (l < 120)
                        ll = "LOW";
                    else
                        ll = "HIGH";
                    tvLevel.setText(ll);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "EXCEPTION OCCURED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    class HeartAsync extends AsyncTask<String,Void,JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject json = makeHttpRequest(strings[0], null, null, null, null, context);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);
            if (jsonObject == null) {
                Toast.makeText(context, "CONNECTION ERROR", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONArray jsonArray = null;
                try {
                    jsonArray = jsonObject.getJSONArray("array");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        heart = object.getString("heart");
                    }
                    tvCurrentHeart.setText(heart);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "EXCEPTION OCCURED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
