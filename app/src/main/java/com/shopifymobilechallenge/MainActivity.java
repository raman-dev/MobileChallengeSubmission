package com.shopifymobilechallenge;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CODE = 99;
    private final String urlString = "https://shopicruit.myshopify.com/admin/products.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6";

    //grid sizes
    //need to match 10 pairs to win so must be more than 20 deck but 10 unique
    //so have 24 images
    //12 unique

    private Context context;
    private GameStateManager gsm;

    private ArrayList<ImageCard> deck;

    private LinearLayout linearLayout;
    private Chronometer chronometer;
    private ProgressDialog progressDialog;
    private Bitmap defaultImage = null;

    private MainMenuDialogFragment mainMenuDialogFragment;
    private RulesDialogFragment rulesDialogFragment;
    private SettingsDialogFragment settingsDialogFragment;
    private WinDialogFragment winDialogFragment;

    private Button shuffleButton;
    private TextView remainingPairLabel;
    private TextView fastestTimeLabel;
    private TextView pairFoundLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        //json object with key products
        configureProgressDialog();
        //and json array after words
        linearLayout = findViewById(R.id.linearLayout);
        chronometer = findViewById(R.id.countUpChronometer);
        remainingPairLabel = findViewById(R.id.RemainingPairLabel);
        pairFoundLabel = findViewById(R.id.PairFoundLabel);
        shuffleButton = findViewById(R.id.ShuffleButton);

        deck = new ArrayList<>();
        mainMenuDialogFragment = new MainMenuDialogFragment(this);
        rulesDialogFragment = new RulesDialogFragment();
        settingsDialogFragment = new SettingsDialogFragment();
        winDialogFragment = new WinDialogFragment(this);
        winDialogFragment.setCancelable(false);

        //get the highest score
        //SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        //sharedPreferences.getString(GameStateManager.HIGH_SCORE_KEY,"00:00");
    }

    private void configureProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Game...");
        progressDialog.setMessage("Downloading Images...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(50);//we know there are max 50 unique products
        progressDialog.setCancelable(false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                int result = wifiManager.getWifiState();
                if(result != WifiManager.WIFI_STATE_ENABLED){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Turn On Wifi to Play");
                    builder.setOnDismissListener(dialog -> {
                        dialog.dismiss();
                        finish();
                    });
                }
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        int result = wifiManager.getWifiState();
        if(result != WifiManager.WIFI_STATE_ENABLED){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Turn On Wifi to Play");
            builder.setOnDismissListener(dialog -> {
                dialog.dismiss();
                finish();
            });
            builder.create().show();
        }else{
            //if the deck is empty load the deck
            if(deck.isEmpty()){
                new LoadProductTask().execute(urlString);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE|
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.PlayButton:
                mainMenuDialogFragment.dismiss();
                gsm.StartGame();
                hideStatusBar();
                break;
            case R.id.RulesButton:
                //show a rules dialog
                //that contains the rules of my game
                if(!rulesDialogFragment.isAdded()){
                    if(!rulesDialogFragment.isVisible()){
                        rulesDialogFragment.show(getSupportFragmentManager(),"RulesDialogFragment");
                    }
                }
                break;
            case R.id.SettingsButton:
                //a settings dialog that shows
                //configurable grid size, three sizes
                //timed mode?
                if(!settingsDialogFragment.isAdded()){
                    if(!settingsDialogFragment.isVisible()){
                        settingsDialogFragment.show(getSupportFragmentManager(),"SettingsDialogFragment");
                    }
                }
                break;

            case R.id.PlayAgainButton:
                winDialogFragment.dismiss();
                gsm.NewGame();
                gsm.StartGame();
                break;

            case R.id.BackToMainButton:
                winDialogFragment.dismiss();
                if(!mainMenuDialogFragment.isAdded()){
                    if(!mainMenuDialogFragment.isVisible()){
                        mainMenuDialogFragment.show(getSupportFragmentManager(),"SettingsDialogFragment");
                    }
                }
                break;
        }
    }

    //                                               input,progress,result
    private class LoadProductTask extends AsyncTask <String,ImageCard,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                URL url = null;
                try {
                    url = new URL(urls[0]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
                int out = bufferedInputStream.read();
                StringBuilder builder = new StringBuilder();
                while(out != -1){
                    builder.append((char)out);
                    out = bufferedInputStream.read();
                }
                connection.disconnect();
                //extract json objects
                try {
                    JSONObject jsonObject = new JSONObject(builder.toString());
                    JSONArray array = (JSONArray) jsonObject.get("products");//the products array
                    int num_products = array.length();//how many products there are
                    System.out.println("num_products =>"+num_products);
                    for (int i = 0; i < num_products; i++) {
                        JSONObject product = array.getJSONObject(i);
                        String product_id = product.getString("id");
                        if(product_id.equals("2759155139")){//skip a product that has different id but same image
                            continue;
                        }
                        String image_key = "image";
                        JSONObject image = (JSONObject) product.get(image_key);
                        String image_src = image.getString("src").replace("\\","");
                        //for each product_id and image_src
                        publishProgress(new ImageCard(context,gsm,product_id,image_src,defaultImage));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ImageCard... values) {
            super.onProgressUpdate(values);
            deck.add(values[0]);
            progressDialog.setProgress(progressDialog.getProgress() + 1);
        }

        @Override
        protected void onPostExecute(Void value) {
            gsm = new GameStateManager(deck,defaultImage,new Handler(Looper.getMainLooper()));
            gsm.setUiElements(getSupportFragmentManager(),winDialogFragment,linearLayout,chronometer,shuffleButton,pairFoundLabel,remainingPairLabel);

            settingsDialogFragment.setGameStateManager(gsm);
            gsm.NewGame();

            if(progressDialog != null){
                progressDialog.dismiss();
            }
            mainMenuDialogFragment.show(getSupportFragmentManager(),"MainMenu");
        }
    }

}
