package com.aectann.unsplashapitest;

/*
 *
 * Developed by V.A. AS open-source test project
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aectann.unsplashapitest.POJOs.POJOPhotos;
import com.aectann.unsplashapitest.POJOs.POJOSearchPhotos;
import com.aectann.unsplashapitest.adapters.AdapterPhotos;
import com.aectann.unsplashapitest.additional.AppPreferences;
import com.aectann.unsplashapitest.interfaces.UnsplashAPI;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    AdapterPhotos adapterPhotos;

    private String BASE_URL = "https://api.unsplash.com/";
    private final static String TAG = "MainActivity";
    int searchPageCounter = 1;
    String text;
    private List<String> arrayIDs;
    private List<String> arrayURLsRegular;
    private List<String> arrayURLsFull;
    private List<String> arrayURLsRaw;
    private List<String> arrayURLsThumb;

    RecyclerView RVPhotos;
    ProgressBar PBImagesMain;
    ProgressBar PB_load_progress;
    EditText ETSearch;
    RelativeLayout RLFull;
    ImageView IVFull;
    ImageView IVThumb;
    LinearLayout LLSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init variables etc.
        init();
        //setting up activity
        setUpActivity();
    }

    private void init(){
//        String ACCESS_TOKEN = "GZLUzVys5XB0R29S1NEXXkdAqD-2vcWieZi53mCjm0g";    //token for access to API
        String ACCESS_TOKEN = "lfNf2yZ1L5JfcPRfQdCciNj3873t_HDyIsYHsPmspHg";    //token for access to API
        AppPreferences.setAccessKey(this, ACCESS_TOKEN);                   //saving it in the Shared Preferences
        AppPreferences.setSearchingStatus(this, "false");
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        arrayIDs = new ArrayList<>();
        arrayURLsThumb = new ArrayList<>();
        arrayURLsRaw = new ArrayList<>();
        arrayURLsFull = new ArrayList<>();
        arrayURLsRegular = new ArrayList<>();
        RVPhotos = findViewById(R.id.RVPhotos);                           //recyclerview with photos
        PBImagesMain = findViewById(R.id.PBImagesMain);     //progress bar
        PB_load_progress = findViewById(R.id.PB_load_progress);     //progress bar
        ETSearch = findViewById(R.id.ETSearch);     //progress bar
    }

    private void setUpActivity() {
        //download photos from server
        new ATdownloadPhotos().execute();
        //downloadPhotos();

        //initializing adapter, sending param-s
        adapterPhotos = new AdapterPhotos(this, arrayIDs, arrayURLsThumb, arrayURLsRaw, arrayURLsFull, arrayURLsRegular);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        int numberOfColumns = calculateNoOfColumns(this, 120);      //calculating available number of rows
        RVPhotos.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        RVPhotos.setAdapter(adapterPhotos);

        //set up search
        ETSearch.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        AppPreferences.setSearchingStatus(MainActivity.this, "true");
                        clearArrays();      //clean adapter data for new from search
                        text = ETSearch.getText().toString();
                        if (!text.equals("") && !text.equals(" ")) {              //if text not empty
//                            clearArrays();      //clean adapter data for new from search
                            int per_page = 10;
                            new ATSearchPhotos(text, String.valueOf(searchPageCounter), String.valueOf(per_page)).execute();
                            adapterPhotos.notifyDataSetChanged();
                            searchPageCounter++;
                        } else {                                                 //if text is empty
                            AppPreferences.setSearchingStatus(MainActivity.this, "false");
//                            clearArrays();      //clean adapter data for new from search
                            downloadRandomPhotos();
                            adapterPhotos.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.textIsEmpty), Toast.LENGTH_SHORT).show();
            }
                        //hide keyboard after pressing "Done" button
                        View view = this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        return true;
                    }
                    // Return true if you have consumed the action, else false.
                    return false;
                });
    }

    private void downloadRandomPhotos(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UnsplashAPI unsplashAPIObject = retrofit.create(UnsplashAPI.class);
        String ACCESS_TOKEN = AppPreferences.getAccessKey(this);
        String count = "20";
        Call<List<POJOPhotos>> call = unsplashAPIObject.getRandomPhotos("Client-ID " + ACCESS_TOKEN, count);

        //thought about several queries one by one, wrote synchronous variant
        try {
            Response<List<POJOPhotos>> response = call.execute();
            int statusCode = response.code();
            if (statusCode == 200) {
                Log.d(TAG, "response.code() downloadRandomPhotos: " + statusCode);
                for(int i = 0; i <= Objects.requireNonNull(response.body()).size() - 1; i++){
                    arrayIDs.add(response.body().get(i).getId());                           //id photo
                    arrayURLsThumb.add(response.body().get(i).getUrls().getThumb());        //thumb data
                    arrayURLsRaw.add(response.body().get(i).getUrls().getRaw());            //raw   data
                    arrayURLsFull.add(response.body().get(i).getUrls().getFull());          //full  data
                    arrayURLsRegular.add(response.body().get(i).getUrls().getRegular());    //regular  data
                }
            } else{
                //everything except of 200
                Log.d(TAG, "response.code() downloadRandomPhotos: " + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchPhotos(String searchTerms, String page, String per_page) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UnsplashAPI unsplashAPIObject2 = retrofit.create(UnsplashAPI.class);
        String ACCESS_TOKEN = AppPreferences.getAccessKey(this);
        Call<POJOSearchPhotos> call = unsplashAPIObject2.searchPhotos("Client-ID " + ACCESS_TOKEN, searchTerms, page, per_page);
        try {
            Response<POJOSearchPhotos> response = call.execute();
            int statusCode = response.code();
//            String isDownloadAvailable = AppPreferences.getIsDownloadAvailable(this);
//            if(!isDownloadAvailable.equals("false")){
                if (statusCode == 200){      //infinity queries to API will starts
                    if(Objects.requireNonNull(response.body()).getTotalPages() <= Integer.parseInt(page)){
                        Log.d(TAG, "Last load session");
                        AppPreferences.setIsDownloadAvailable(this, "false");
                    }
                    Log.d(TAG, "response.code() searchPhotos: " + 200);
                    for (int i = 0; i <= Objects.requireNonNull(response.body()).getResults().size() - 1; i++) {
                        arrayIDs.add(response.body().getResults().get(i).getId());                              //id photo
                        arrayURLsThumb.add(response.body().getResults().get(i).getUrls().getThumb());           //thumb data
                        arrayURLsRaw.add(response.body().getResults().get(i).getUrls().getRaw());               //raw   data
                        arrayURLsFull.add(response.body().getResults().get(i).getUrls().getFull());             //full  data
                        arrayURLsRegular.add(response.body().getResults().get(i).getUrls().getRegular());       //full  data
                    }
                } else{
                    //everything except of 200
                    Log.d(TAG, "response.code() searchPhotos: " + statusCode);
                }
//            } else{
//                Log.d(TAG, "Dowload isn't available due to the end of images.");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadMoreRandomPhotos(){
        new ATdownloadPhotos().execute();
    }

    public void downloadMoreSearchPhotos(){
        int per_page = 10;
        new ATSearchPhotos(text, String.valueOf(searchPageCounter), String.valueOf(per_page)).execute();
    }

    //call it from adapter - fullscreen image with download button
    public void itemClickHandler(/*String id, */String imageURLThumb,/* String imageURLFull,*/ String imageURLRegular){
        RLFull = findViewById(R.id.RLFull);
        RLFull.setVisibility(View.VISIBLE);
        IVFull = findViewById(R.id.IVFull);
        IVThumb = findViewById(R.id.IVThumb);
        LLSave = findViewById(R.id.LLSave);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PB_load_progress.setVisibility(View.VISIBLE);
        Picasso.get().load(imageURLThumb).into(IVThumb, new Callback() {
            @Override
            public void onSuccess() {
                //users with API 21 etc. with light devices may load this for a long, so let's speak with them and other
                Toast.makeText(MainActivity.this, getResources().getString(R.string.pleaseWait), Toast.LENGTH_SHORT).show();
                Picasso.get().load(imageURLRegular).into(IVFull, new Callback() {
                    @Override
                    public void onSuccess() {
                        PB_load_progress.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        PB_load_progress.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.errorWhileLoading), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                PB_load_progress.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, getResources().getString(R.string.errorWhileLoading), Toast.LENGTH_SHORT).show();
            }
        });

        LLSave.setOnClickListener(v->{
            SaveImage(this, imageURLRegular/*, id*/);
//            Toast.makeText(MainActivity.this, getResources().getString(R.string.savingWait), Toast.LENGTH_SHORT).show();
        });
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }
    public void clearArrays(){
        arrayIDs.clear();
        arrayURLsThumb.clear();
        arrayURLsRaw.clear();
        arrayURLsFull.clear();
    }

    private static void SaveImage(final Context context, final String MyUrl/*, final String id*/){
        final ProgressDialog progress = new ProgressDialog(context);
        @SuppressWarnings("ResultOfMethodCallIgnored")
        class SaveThisImage extends AsyncTask<Void, Void, Void> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setTitle("Processing");
                progress.setMessage("Please Wait...");
                progress.setCancelable(false);
                progress.show();
            }
            @Override
            protected Void doInBackground(Void... arg0) {
                try{

                    File sdCard = Environment.getExternalStorageDirectory();
                    @SuppressLint("DefaultLocale") String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    File dir = new File(sdCard.getAbsolutePath() + "/Image viewer");
                    dir.mkdirs();
                    final File myImageFile = new File(dir, fileName); // Create image file
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(myImageFile);
                        Bitmap bitmap = Picasso.get().load(MyUrl).get();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(myImageFile));
                        context.sendBroadcast(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            Objects.requireNonNull(fos).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(progress.isShowing()){
                    progress.dismiss();
                }
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            }
        }
        SaveThisImage shareimg = new SaveThisImage();
        shareimg.execute();
    }

    @Override
    public void onBackPressed(){
        if(RLFull.getVisibility() == View.VISIBLE){
            RLFull.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else{
            super.onBackPressed();
        }
//        if(onBackPressedCounter == 2 ){
//            super.onBackPressed();
//        } else{
//            RLFull.setVisibility(View.GONE);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            Toast.makeText(MainActivity.this, getResources().getString(R.string.acceptExit), Toast.LENGTH_LONG).show();
//            onBackPressedCounter++;
//        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ATdownloadPhotos extends AsyncTask<Void, Void, Void> {

        public ATdownloadPhotos(){
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PBImagesMain.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            downloadRandomPhotos();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PBImagesMain.setVisibility(View.INVISIBLE);
            adapterPhotos.notifyDataSetChanged();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ATSearchPhotos extends AsyncTask<Void, Void, Void> {

        String mSearchTerms;
        String mPage;
        String mPer_page;

        public ATSearchPhotos(String searchTerms, String page, String per_page){
            mSearchTerms = searchTerms;
            mPage = page;
            mPer_page = per_page;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PBImagesMain.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            searchPhotos(mSearchTerms, mPage, mPer_page);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            PBImagesMain.setVisibility(View.INVISIBLE);
            adapterPhotos.notifyDataSetChanged();
        }
    }
}