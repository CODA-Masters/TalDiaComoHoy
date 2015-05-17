package materialdesign.codamasters.com.materialdesign;

/**
 * Created by Juan on 17/05/2015.
 */

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class InfoActivity extends ListActivity {

    private ProgressDialog pDialog;

    // URL to get contacts JSON
    //private static String url_en = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&titles=";
    private static String url_es = "http://es.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=Álvaro_Martínez_Sevilla";

    private String title; // EN --> February_25; ES --> 25_de_febrero

    // JSON Node names
    private static final String TAG_QUERY = "query";
    private static final String TAG_PAGES = "pages";
    private static final String TAG_CONTENT = "contentmodel";


    // contacts JSONArray
    JSONArray results = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> resultList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        resultList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        // Listview on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String cost = ((TextView) view.findViewById(R.id.email)) .getText().toString();
                String description = ((TextView) view.findViewById(R.id.mobile)) .getText().toString();

            }
        });

        // Calling async task to get json
        new GetResults().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetResults extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(InfoActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url_es, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    results = jsonObj.getJSONArray(TAG_QUERY);

                    // looping through All Contacts
                    for (int i = 0; i < results.length(); i++) {

                        JSONObject c = results.getJSONObject(i);
                        //String content = c.getString(TAG_CONTENT);
                        String content = c.toString();

                        // tmp hashmap for single contact
                        HashMap<String, String> result = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        result.put(TAG_CONTENT, content);

                        // adding contact to contact list
                        resultList.add(result);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    InfoActivity.this, resultList,
                    R.layout.list_item, new String[] { TAG_CONTENT }, new int[] { R.id.name});

            setListAdapter(adapter);
        }

    }

}