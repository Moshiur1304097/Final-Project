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

public class MainActivity extends AppCompatActivity {
    ChartHelper mChart;
    LineChart chart;
    Button btPairedBlue;
    Button btConnectdBlue;
    Button btSendBlue;
    Button btReadBlue;
    Button btResult;
    Button btInsertHeart;
    Button btDataHeart;
    Button btMail;
    TextView tv;
    TextView tvBpm;
    TextView tvSo2;
    TextView tvDate;
    TextView tvTime;
    TextView tvValue;
    BluetoothSocket mSocket = null;
    BluetoothDevice mDevice;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    Context context;
    Glucose[] arrayGlucose;
    private static final int READ_REQUEST_CODE = 42;
    public static final int MESSAGE_READ = 1;
    Handler handler;
    Handler handlerChart;
    private String urlG = "http://192.168.43.166/healthCare4/insertGlucose.php";
    private String urlH = "http://192.168.43.166/healthCare4/insertHeart.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = (LineChart) findViewById(R.id.chart);
        mChart = new ChartHelper(chart);
        btConnectdBlue = (Button) findViewById(R.id.btConnectBlue);
        btPairedBlue = (Button) findViewById(R.id.btPairBlue);
        btSendBlue = (Button) findViewById(R.id.btSendBlue);
        btReadBlue = (Button) findViewById(R.id.btRead);
        btResult = (Button) findViewById(R.id.btResult);
        btInsertHeart = (Button) findViewById(R.id.btInsertHeart);
        btDataHeart = (Button) findViewById(R.id.btDataHeart);
        btMail = (Button) findViewById(R.id.btMail);
        tv = (TextView) findViewById(R.id.tv);
        tvBpm = (TextView) findViewById(R.id.bpm);
        tvSo2 = (TextView) findViewById(R.id.so2);
        tvDate = (TextView) findViewById(R.id.date);
        tvTime = (TextView) findViewById(R.id.time);
        tvValue = (TextView) findViewById(R.id.glucose);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context = this;
        arrayGlucose = new Glucose[100];
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        final StringBuilder stb = new StringBuilder();
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                byte[] readBuffer = (byte[]) msg.obj;
                String str = new String(readBuffer, 0, msg.arg1);
//                String str = new String(readBuffer, 0, 3);
                tv.setText("data:"+str);
                stb.append(" "+str+" ");
                String r = "10";
                if (str.charAt(0) >= '1' && str.charAt(0) <= '9')
                    r = str;
                handlerChart.obtainMessage(MESSAGE_READ, r.length(), -1, r).sendToTarget();
            }
        };
        handlerChart = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String readBuffer = (String) msg.obj;
//                String str = new String(readBuffer, 0, msg.arg1);
//                String str = new String(readBuffer, 0, 3);
                StringTokenizer stt = new StringTokenizer(readBuffer);
                while (stt.hasMoreElements()){
                    mChart.addEntry(Float.valueOf(stt.nextToken()));
                }

            }
        };
        final String[] arrayResult = new String[3];
        final String[] result = new String[2];
        btResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = new String(stb);
                StringTokenizer st = new StringTokenizer(s);
                while(st.hasMoreElements()){
                    String tokenString = st.nextToken();
                    if (tokenString.equals("BPM"))
                        arrayResult[0] = st.nextToken();
                    else if (tokenString.equals("SO2") )
                        arrayResult[1] = st.nextToken();
                    else if (tokenString.equals("TEMP"))
                        arrayResult[2] = st.nextToken();
                    else if(tokenString.equals("Heart"))
                        result[0] = st.nextToken();
                    else if (tokenString.equals("Signal"))
                        result[1] = st.nextToken();
//                    tvBpm.setText(result[0]);
//                    tvSo2.setText(result[1]);
                }
//                tvBpm.setText(arrayResult[0]);
//                tvSo2.setText(arrayResult[1]);
//                tvTem.setText(arrayResult[2]);
                tvBpm.setText(result[0]);
                tvSo2.setText(result[1]);
//                mChart.addEntry(Float.valueOf(result[1]));
            }
        });
        final ReadAsync[] readAsync = new ReadAsync[1];
        btInsertHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String s1 = arrayResult[0];
//                String s2 = arrayResult[1];
//                String s3 = arrayResult[2];
//                new InsertAsync().execute(url,arrayResult[0], arrayResult[1], arrayResult[2]);
                readAsync[0].cancel(true);
                new InsertAsync().execute(urlH,result[0], null, null,null,null);
                tvBpm.setText(result[0]);
                tvSo2.setText(result[1]);
            }
        });
        btPairedBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        if (deviceName.equals("HC-05")){
                            mDevice = device;
                            Toast.makeText(context, "Paired with HC-05", Toast.LENGTH_SHORT).show();
                            tv.setText("PAired with HC-05");
                        }

                    }
                }
            }
        });
        btConnectdBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("Connecting..");
                if (mSocket == null) {
                    new ConnectAsync().execute();
                }
                else if (!mSocket.isConnected()) {
                    new ConnectAsync().execute();
                }
                else if (mSocket.isConnected())
                    tv.setText("Already Connected");
            }
        });
        btSendBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte b = '1';
                if (mSocket != null && mSocket.isConnected())
                    new WriteAsync().execute(b);
                else
                    Toast.makeText(context, "Not connected Cant write", Toast.LENGTH_SHORT).show();
            }
        });

        btReadBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAsync[0] = new ReadAsync();
                readAsync[0].execute();
            }
        });
        btDataHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListActivity.class);
                startActivity(intent);
            }
        });
        btMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Main3Activity.class);
                i.putExtra("Heart", result[0]);
                startActivity(i);
            }
        });
    }

    class ConnectAsync extends  AsyncTask<Void, Void, Void>{
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        @Override
        protected Void doInBackground(Void...voids) {
            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothAdapter.cancelDiscovery();
                if (mSocket == null || !mSocket.isConnected()) {
                    mSocket.connect();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mSocket != null && mSocket.isConnected()) {
                tv.setText("Connected");
            }
            else {
                tv.setText("Connection failed");
                mSocket = null;
            }
        }
    }
    class ReadAsync extends AsyncTask<Void, Void, byte[]>{
        InputStream in;
        byte[] b;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                in = mSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected byte[] doInBackground(Void...voids ) {
            b = new byte[1024];
            int size;
            while(in != null){
                try {
                    size = in.read(b);
                    handler.obtainMessage(MESSAGE_READ, size, -1, b).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    in = null;
                    break;
                }
            }

            return b;
        }
        @Override
        protected void onPostExecute(byte[] b) {
            super.onPostExecute(b);
            if (in == null)
                Toast.makeText(context, "No input Stream..", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "Trying to read..", Toast.LENGTH_SHORT).show();
            String str = new String(" Stop REading ");
//            tv.setText(str);
        }
    }
    class WriteAsync extends AsyncTask<Byte, Void, Void>{
        boolean bool = false;
        OutputStream out;

        {
            try {
                out = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Byte...bytes) {
                try {

                    String str = "1";
                    byte[] name = str.getBytes();
                    out.write(name);
                    out.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    bool = true;
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!bool) {
                tv.setText("Sendt data");
            }
            else
                tv.setText("Data sending failure");
        }
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
                Toast.makeText(context, "Successfully Inserted", Toast.LENGTH_SHORT).show();
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
        if (mSocket != null){
            try {
                mSocket.close();
                tv.setText("Disconnected");
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arrayGlucose[0] != null){
            tvDate.setText(arrayGlucose[0].getDate());
            tvTime.setText(arrayGlucose[0].getTime());
            tvValue.setText(arrayGlucose[0].getValue());
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
