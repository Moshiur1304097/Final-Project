package com.example.v.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import static com.example.v.bluetooth.QeryUtils.makeHttpRequest;

public class Main2Activity extends AppCompatActivity {
    Button btInsertGlucose;
    Button btDataGlucose;
    Button btGlucose;
    Button btMail;
    TextView tvDate;
    TextView tvTime;
    TextView tvValue;
    Context context;
    Glucose[] arrayGlucose;
    private static final int READ_REQUEST_CODE = 42;
    public static final int MESSAGE_READ = 1;
    private String urlG = "http://192.168.43.166/healthCare4/insertGlucose.php";
    private String urlH = "http://192.168.43.166/healthCare4/insertHeart.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btInsertGlucose = (Button) findViewById(R.id.btInsertGlucose);
        btDataGlucose = (Button) findViewById(R.id.btDataGlucose);
        btGlucose = (Button) findViewById(R.id.btGlucose);
        btMail = (Button) findViewById(R.id.btMail);
        tvDate = (TextView) findViewById(R.id.date);
        tvTime = (TextView) findViewById(R.id.time);
        tvValue = (TextView) findViewById(R.id.glucose);
        context = this;
        arrayGlucose = new Glucose[100];
        btInsertGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for (int i = 0; i < arrayGlucose.length; i++){
                int i = 0;
                if (arrayGlucose[i] != null)
                    new InsertAsync().execute(urlG, null, arrayGlucose[i].getValue(),arrayGlucose[i].getDate(),arrayGlucose[i].getTime());
//                }
            }
        });
        btDataGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListActivity2.class);
                startActivity(intent);
            }
        });
        btGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GlucoseAsync().execute();
            }
        });
        btMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    class InsertAsync extends AsyncTask<String,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject json = makeHttpRequest(strings[0], strings[1], strings[2], strings[3],strings[4], context);
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject == null)
                Toast.makeText(context, "CONNECTION ERROR", Toast.LENGTH_SHORT).show();
            else {
                try {
                    Toast.makeText(context, jsonObject.getString("message").toString(), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "EXCEPTION OCCURED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    class GlucoseAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            performFileSearch();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (arrayGlucose[0] != null){
                tvDate.setText(arrayGlucose[0].getDate());
                tvTime.setText(arrayGlucose[0].getTime());
                tvValue.setText(arrayGlucose[0].getValue());

            }
            else Log.i("MainActivity", "array null");
        }
    }
    public void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("MainActivity", "Uri: " + uri.toString());
                int count = 0;
                try {
                    String str = readTextFromUri(uri);
                    Log.i("MainActivity", str);
                    StringTokenizer stt = new StringTokenizer(str, ",");
                    while (stt.hasMoreElements()){
                        String ss = stt.nextToken().trim();
                        Log.i("MainActivity", ss);
                        StringTokenizer st = new StringTokenizer(ss);
                        String s = null;
                        while (st.hasMoreElements()){
                            s = st.nextToken();
                        }
                        if (s != null && s.equals("2018")){
                            arrayGlucose[count] = new Glucose(ss,stt.nextToken().trim(),stt.nextToken().trim());
                            count++;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    Log.i("MainActivity", ""+count);
                }
            }
            else
                Log.i("MainActivity", "Uri: null");
        }
    }
    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arrayGlucose[0] != null){
            tvDate.setText(arrayGlucose[0].getDate());
            tvTime.setText(arrayGlucose[0].getTime());
            tvValue.setText(arrayGlucose[0].getValue());
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);SharedPreferences.Editor editor = pref.edit();
            editor.putString("Glucose", arrayGlucose[0].getValue());
        }
        else Log.i("MainActivity", "array null");
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
}
