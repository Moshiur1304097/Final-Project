package com.example.v.bluetooth;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.v.bluetooth.QeryUtils.makeHttpRequest;

public class ListActivity2 extends AppCompatActivity {


    private String url = "http://192.168.43.166/healthCare4/showGlucose.php";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        context = this;
        Log.e("QUERYUTILS", "aisiiiiiiiiii");
        new ShowAsync().execute(url);
    }

    class ShowAsync extends AsyncTask<String,Void,JSONObject> {

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
                final ArrayList<Glucose> options = new ArrayList<Glucose>();
                JSONArray jsonArray = null;
                try {
                    jsonArray = jsonObject.getJSONArray("array");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        options.add(new Glucose(object.getString("date"),
                                object.getString("time"),
                                object.getString("glucose")));
                    }
                    ListView listView = (ListView) findViewById(R.id.list);
                    AdapterActivity adapterActivity = new AdapterActivity(context, R.id.list, options);
                    listView.setAdapter(adapterActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "EXCEPTION OCCURED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
