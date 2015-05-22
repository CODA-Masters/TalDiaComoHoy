package com.codamasters.taldiacomohoy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ListActivity{

    // Animations
    private View mFab;
    private FrameLayout mFabContainer;
    private LinearLayout mControlsContainer;
    private LinearLayout listContainer;
    private ListView lv;

    public final static float SCALE_FACTOR = 13f;
    public final static int ANIMATION_DURATION = 300;
    public final static int MINIMUN_X_DISTANCE = 200;

    private boolean mRevealFlag;
    private float mFabSize;
    private float fabX, fabY;


    // Information

    private ProgressDialog pDialog;


    // JSON Node names
    private static final String TAG_QUERY = "query";
    private static final String TAG_CONTENT = "contentmodel";
    private static final String TAG_YEAR = "year";
    private static final String TAG_LINK = "link";

    private String[] texto;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> resultList;


    private ImageButton button;
    private DatePicker datePicker;
    private FrameLayout toolbar;


    private String[] months_es = {"enero","febrero","marzo","abril","mayo","junio","julio", "agosto","septiembre","octubre","noviembre","diciembre"};
    private String[] months_en = {"January", "February", "March", "April","May","June","July","August","September","October","November","December"};

    public static String text_uri_es, text_uri_en;
    public static String text_title_es, text_title_en;
    private static String url_es, url_en;
    private TextView tv;

    private static final String INIT_KEY_ES_1 = "==Acontecimientos==";
    private static final String INIT_KEY_EN_1 = "==Events==";

    private static final String INIT_KEY_ES_2 = "== Acontecimientos ==";
    private static final String INIT_KEY_EN_2 = "== Events ==";

    private static final String END_KEY_ES_1 = "==Nacimientos==";
    private static final String END_KEY_EN_1 = "==Births==";

    private static final String END_KEY_ES_2 = "== Nacimientos ==";
    private static final String END_KEY_EN_2 = "== Births ==";

    private static final String DATA_KEY_ES = "Archivo";
    private static final String DATA_KEY_EN = "File";

    private static final String YEAR_ES = "Año ";
    private static final String YEAR_EN = "Year ";

    private static final String SPLIT_KEY_ES = ":";
    private static final String SPLIT_KEY_EN = "&ndash;";

    private String init_key_1, init_key_2, end_key_1, end_key_2, data_key, split_key, url, text_title, title_year;


    private InterstitialAd interstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.intersticial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
            @Override
            public void onAdClosed() {
            }
        });


        mFab = findViewById(R.id.fab);
        fabX = mFab.getX();
        fabY = mFab.getY();
        mFabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);

        mFabContainer = (FrameLayout) findViewById(R.id.fab_container);
        mControlsContainer = (LinearLayout) findViewById(R.id.media_controls_container);
        listContainer = (LinearLayout) findViewById(R.id.contenedor_lista);

        button = (ImageButton) findViewById(R.id.buscar);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        toolbar = (FrameLayout) findViewById(R.id.toolbar);

        resultList = new ArrayList<HashMap<String, String>>();


        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();

        datePicker.setVisibility(View.GONE);
        button.setVisibility(View.GONE);

        text_uri_es = text_title_es = "";
        text_uri_en = text_title_es = "";

        text_uri_es = day + "_de_" + months_es[month];
        text_title_es = day + " de " + months_es[month];

        text_uri_en = months_en[month] + "_" + day;
        text_title_en = months_en[month] + " " + day;

        url_es = "http://es.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + text_uri_es;
        url_en = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + text_uri_en;


        switch(Locale.getDefault().getDisplayLanguage()){
            case "español": url = url_es;
                text_title = text_title_es;
                init_key_1 = INIT_KEY_ES_1;
                end_key_1 = END_KEY_ES_1;
                init_key_2 = INIT_KEY_ES_2;
                end_key_2 = END_KEY_ES_2;
                data_key = DATA_KEY_ES;
                split_key = SPLIT_KEY_ES;
                title_year = YEAR_ES;

                break;
            case "english": url = url_en;
                text_title = text_title_en;
                init_key_1 = INIT_KEY_EN_1;
                end_key_1 = END_KEY_EN_1;
                init_key_2 = INIT_KEY_EN_2;
                end_key_2 = END_KEY_EN_2;
                data_key = DATA_KEY_EN;
                split_key = SPLIT_KEY_EN;
                title_year = YEAR_EN;
                break;
            default: url = url_en;
                text_title = text_title_en;
                init_key_1 = INIT_KEY_EN_1;
                end_key_1 = END_KEY_EN_1;
                init_key_2 = INIT_KEY_EN_2;
                end_key_2 = END_KEY_EN_2;
                data_key = DATA_KEY_EN;
                split_key = SPLIT_KEY_EN;
                title_year = YEAR_EN;
        }

        tv = (TextView) findViewById(R.id.date);
        tv.setText(text_title);


        ListView lv = getListView();

        // Listview on item click listener

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // getting values from selected ListItem
                String year = ((TextView) view.findViewById(R.id.year)).getText().toString();
                String content = ((TextView) view.findViewById(R.id.content)).getText().toString();


                shareAnswer(text_title + " - " + year + "\n\n" + content + "\n\n" + getString(R.string.share_sign));
                return true;
            }
        });


        if (haveNetworkConnection()) {
            new GetResults().execute();

        } else {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle(getString(R.string.internet_error_title));
            builder1.setMessage(getString(R.string.internet_error_description));

            builder1.setNegativeButton(getString(R.string.action_wifi), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder1.setPositiveButton(getString(R.string.action_mobile_data), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(
                            "com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    //startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    startActivity(intent);
                }
            });
            builder1.setNeutralButton(getString(R.string.action_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

    }

    public void showOrLoadInterstital() {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    }
                    else {
                        AdRequest interstitialRequest = new AdRequest.Builder().build();
                        interstitialAd.loadAd(interstitialRequest);
                    }


                }
            });
        } catch (Exception e) {
        }
    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public void onFabPressed(View view) {

        final float startX = mFab.getX();

        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        path.curveTo(-200, 200, -400, 100, -600, 50);

        final ObjectAnimator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());

        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                if (Math.abs(startX - mFab.getX()) > MINIMUN_X_DISTANCE) {

                    if (!mRevealFlag) {

                        mFab.animate()
                                .scaleXBy(SCALE_FACTOR)
                                .scaleYBy(SCALE_FACTOR)
                                .setListener(mEndRevealListener)
                                .setDuration(ANIMATION_DURATION);

                        mRevealFlag = true;
                    }
                }
            }
        });

    }

    private AnimatorListenerAdapter mEndRevealListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {

            super.onAnimationEnd(animation);

            toolbar.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            listContainer.setVisibility(View.GONE);
            datePicker.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);


            mFabContainer.setBackgroundColor(getResources()
                    .getColor(R.color.brand_accent));

            for (int i = 0; i < mControlsContainer.getChildCount(); i++) {

                View v = mControlsContainer.getChildAt(i);
                ViewPropertyAnimator animator = v.animate()
                        .scaleX(1).scaleY(1)
                        .setDuration(ANIMATION_DURATION);

                animator.setStartDelay(i * 50);
                animator.start();
            }
        }
    };


    /**
     * We need this setter to translate between the information the animator
     * produces (a new "PathPoint" describing the current animated location)
     * and the information that the button requires (an xy location). The
     * setter will be called by the ObjectAnimator given the 'fabLoc'
     * property string.
     */
    public void setFabLoc(PathPoint newLoc) {

        mFab.setTranslationX(newLoc.mX);


        if (mRevealFlag)
            mFab.setTranslationY(newLoc.mY - (mFabSize / 2));
        else
            mFab.setTranslationY(newLoc.mY);
    }

    public void search(View view) {


        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();

        datePicker.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        listContainer.setVisibility(View.VISIBLE);

        text_uri_es = text_title_es = "";
        text_uri_en = text_title_es = "";

        text_uri_es = day + "_de_" + months_es[month];
        text_title_es = day + " de " + months_es[month];

        text_uri_en = months_en[month] + "_" + day;
        text_title_en = months_en[month] + " " + day;

        url_es = "http://es.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + text_uri_es;
        url_en = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + text_uri_en;


        switch(Locale.getDefault().getDisplayLanguage()){
            case "español": url = url_es;
                text_title = text_title_es;
                break;
            case "english": url = url_en;
                text_title = text_title_en;
                break;
            default:    url = url_en;
                text_title = text_title_en;
        }

        tv.setText(text_title);


        if (haveNetworkConnection()) {
            new GetResults().execute();

            ListView lv = getListView();

            // Listview on item click listener

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    // getting values from selected ListItem
                    String year = ((TextView) view.findViewById(R.id.year)).getText().toString();
                    String content = ((TextView) view.findViewById(R.id.content)).getText().toString();

                    shareAnswer(text_title + " - " + year + "\n\n" + content + "\n\n" + getString(R.string.share_sign));
                    return true;
                }
            });

        } else {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle(getString(R.string.internet_error_title));
            builder1.setMessage(getString(R.string.internet_error_description));

            builder1.setNegativeButton(getString(R.string.action_wifi), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            builder1.setPositiveButton(getString(R.string.action_mobile_data), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(
                            "com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    //startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                    startActivity(intent);
                }
            });
            builder1.setNeutralButton(getString(R.string.action_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }


        for (int i = 0; i < mControlsContainer.getChildCount(); i++) {

            View v = mControlsContainer.getChildAt(i);
            ViewPropertyAnimator animator = v.animate()
                    .scaleX(0).scaleY(0)
                    .setDuration(ANIMATION_DURATION);

            animator.setStartDelay(i * 50);
            animator.start();
        }


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        mFab.setScaleX(1);
        mFab.setScaleY(1);
        final float scale = this.getResources().getDisplayMetrics().density;
        int dps = 16;
        int pixels = (int) (dps * scale + 0.5f);
        mFab.setX(fabX + width - mFabSize - pixels);
        mFab.setY(fabY);

        mFab.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);

        mFabContainer.setBackgroundColor(0x00000000);

        mRevealFlag = false;

    }

    private void shareAnswer(String answer) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, answer);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
    }


    private class GetResults extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();

            resultList.clear();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject info = jsonObj.getJSONObject(TAG_QUERY);

                    Map<String, String> out = new HashMap<String, String>();

                    parse(info, out);

                    JSONArray aux = new JSONArray(out.get("revisions"));
                    info = aux.getJSONObject(0);

                    String content = info.getString("*");

                    BufferedReader bufReader = new BufferedReader(new StringReader(content));

                    String line = null;
                    ArrayList<String> lines = new ArrayList<>();
                    boolean read = false;

                    while ((line = bufReader.readLine()) != null) {

                        if (line.contains(init_key_1) || line.contains(init_key_2) )
                            read = true;

                        if (line.contains(end_key_1) || line.contains(end_key_2))
                            read = false;

                        if (read) {
                            if (!line.contains(data_key))
                                lines.add(line);
                        }

                    }

                    for (int i = 2; i < lines.size() - 1; i++) {

                        HashMap<String, String> result = new HashMap<String, String>();
                        String[] splited_lines = lines.get(i).split(split_key);

                        if (splited_lines != null) {

                            String year = splited_lines[0];

                            texto = year.split("\\*\\ *(\\[\\[)*");
                            year = "";

                            for (String tex : texto) {
                                year = year.concat(tex);
                            }

                            if (year.contains("]]")) {
                                year = year.replace("]]", "");
                            }

                            if (year.contains("|")) {
                                texto = year.split("\\|");
                                year = texto[0];
                            }

                            texto = year.split("\\(.*");
                            year = texto[0];


                            result.put(TAG_YEAR, title_year+year);


                            if (splited_lines.length > 1) {

                                String text = "";

                                for (int z = 1; z < splited_lines.length; z++) {
                                    text = text.concat(splited_lines[z]);
                                    if (z != splited_lines.length - 1) {
                                        text = text.concat(":");
                                    }
                                }


                                String enlace = text;
                                String[] cadenas = enlace.split(".*\\[\\[");
                                enlace = "";

                                for (String string : cadenas) {
                                    enlace = enlace.concat(string);
                                }

                                cadenas = enlace.split("\\]\\].*");
                                enlace = "";

                                for (String string : cadenas) {
                                    enlace = enlace.concat(string);
                                }


                                // Problema de la hora
                                /*
                                for(int j=1; i<splited_lines.length; j++){
                                    text = text.concat(splited_lines[j]);
                                }
                                */

                                texto = text.split("\\[\\[[^\\]\\]]*\\|");
                                text = "";

                                for (String tex : texto) {
                                    text = text.concat(tex);
                                }

                                texto = text.split("\\[\\[");
                                text = "";

                                for (String tex : texto) {
                                    text = text.concat(tex);
                                }

                                texto = text.split("\\]\\]");
                                text = "";

                                for (String tex : texto) {
                                    text = text.concat(tex);
                                }

                                texto = text.split("\\<.*\\>.*");
                                text = "";

                                for (String tex : texto) {
                                    text = text.concat(tex);
                                }

                                texto = text.split("\\{\\{.*\\}\\}(,)*");
                                text = "";


                                for (String tex : texto) {
                                    text = text.concat(tex);
                                }

                                text = text.replace("&nbsp;", " ");
                                text = text.substring(1);

                                char[] stringArray = text.trim().toCharArray();
                                if (stringArray.length > 0){
                                    stringArray[0] = Character.toUpperCase(stringArray[0]);
                                    text = new String(stringArray);
                                }

                                result.put(TAG_CONTENT, text);
                                result.put(TAG_LINK, enlace);

                                resultList.add(result);

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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

            Collections.reverse(resultList);

            ListAdapter adapter = new SimpleAdapter(MainActivity.this, resultList , R.layout.list_item, new String[]{TAG_YEAR, TAG_CONTENT, TAG_LINK}, new int[]{R.id.year, R.id.content, R.id.link});

            setListAdapter(adapter);

            showOrLoadInterstital();

        }


        public Map<String, String> parse(JSONObject json, Map<String, String> out) throws JSONException {
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String val = null;
                try {
                    JSONObject value = json.getJSONObject(key);
                    parse(value, out);
                } catch (Exception e) {
                    val = json.getString(key);
                }

                if (val != null) {
                    out.put(key, val);
                }
            }
            return out;
        }

    }

}
