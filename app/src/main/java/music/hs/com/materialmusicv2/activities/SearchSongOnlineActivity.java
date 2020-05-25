package music.hs.com.materialmusicv2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.ShowDatabaseObject;
import music.hs.com.materialmusicv2.SongMetaDataObject;
import music.hs.com.materialmusicv2.adapters.SearchSongOnlineAdapter;
import music.hs.com.materialmusicv2.adapters.ShowDatabaseAdapter;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.hs.com.materialmusicv2.utils.controller.Controller;
import music.hs.com.materialmusicv2.utils.fileutils.FileStatics;
import music.hs.com.materialmusicv2.utils.fileutils.FileUtils;
import music.hs.com.materialmusicv2.utils.fileutils.MediaFile;
import music.hs.com.materialmusicv2.utils.misc.Statics;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.fileutils.FileUtils.copyFileToExternalSDCardForGreaterThanKitkat;
import static music.hs.com.materialmusicv2.utils.fileutils.FileUtils.copyFileToExternalSDCardForKitkat;
import static music.hs.com.materialmusicv2.utils.fileutils.FileUtils.saveToInternalStorage;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_NORMAL;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class SearchSongOnlineActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener,MediaScannerConnection.MediaScannerConnectionClient
{



    private GridView gridView;
    private ArrayList<ShowDatabaseObject> list = new ArrayList<>();
    ShowDatabaseAdapter showDatabaseAdapter;
    ImageButton BackToolbar;
    int colorAccent;
    int colorContrast;
    EditText Search;
    ImageButton SearchButton;
    RelativeLayout SearchLayout;
    SwipeRefreshLayout SwipeRefreshLayout;
    ArrayList<SongMetaDataObject> songList = new ArrayList<>();
    String searchText;
    SearchSongOnlineAdapter searchSongOnlineAdapter;
    private RadioGroup apiRadioGroup;
    int api;
    long ID;
    String path;
    Bitmap imageBitmap;
    public static SearchSongOnlineActivity searchSongOnlineActivity;
    private File file;
    private AudioFile f;
    private Artwork artwork;
    private Tag tag = null;
    ProgressDialog progressDialog;
    private File audio;
    private MediaScannerConnection mediaScannerConnection;
    SongMetaDataObject songMetaDataObject;
    RadioButton Spotify;
    RadioButton Napster;
    private ImageView NoDataFound;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_search_song_online);


        boolean t = ThemeUtils.isThemeDarkOrBlack();

        searchSongOnlineActivity = this;
        int colorPrimary = getThemePrimaryColor(this);
        if (t) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait.....");

        gridView = (GridView) findViewById(R.id.songGrid);
        BackToolbar = (ImageButton) findViewById(R.id.backButton);
        Search = (EditText)findViewById(R.id.search);
        SearchButton =(ImageButton)findViewById(R.id.searchButton);
        SearchLayout = (RelativeLayout)findViewById(R.id.searchLayout);
        SwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        SwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        SwipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        SwipeRefreshLayout.setOnRefreshListener(this);
        Spotify = (RadioButton)findViewById(R.id.spotifyRadio);
        Napster=(RadioButton)findViewById(R.id.napsterRadio);
        NoDataFound = (ImageView) findViewById(R.id.noDataFound);
        apiRadioGroup = (RadioGroup)findViewById(R.id.apiRadioGroup);
        apiRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                gridView.setAdapter(null);
                search();
            }
        });

        colorAccent = MainActivity.colorAccent;
        colorPrimary = MainActivity.colorPrimary;
        colorContrast = ContrastColor(colorPrimary);

        SearchButton.setOnClickListener(this);
        BackToolbar.setOnClickListener(this);

        Spotify.setTextColor(colorContrast);
        Napster.setTextColor(colorContrast);

        ID = getIntent().getLongExtra("id", 0L);
        path = getIntent().getStringExtra("path");
        audio = new File(path);
        f = null;
        try
        {
            f = AudioFileIO.read(audio);
            artwork = f.getTag().getFirstArtwork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Song> songs = QueryUtils.getAllSongs(getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
        Song editSong = Controller.getCurrentSong();
        for( Song s : songs)
        {
            if(s.getId() == ID)
                editSong = s;
        }
        Search.setText(editSong.getName());
        search();


        Search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    gridView.setAdapter(null);
                    search();
                    return true;
                }
                return false;
            }
        });



        NoDataFound.setVisibility(View.GONE);


    }


    @Override
    public void onClick(View v) {

        if(v == BackToolbar)
        {
            onBackPressed();
        }

        if(v == SearchButton)
        {
            gridView.setAdapter(null);
            search();
        }

    }



    @Override
    public void onRefresh()
    {
        gridView.setAdapter(null);
        search();
    }


    public class LoadData extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            URL urlObj;
            HttpURLConnection urlConnection = null;
            try {
                songList.clear();
                String t = searchText;
                String url_str = "";
                if( api == 2)
                {
                    t = t.replaceAll(" ","+");
                    url_str = "https://hs-music-player.herokuapp.com/getSongDataFromNapster?query=" + t;

                }
                else
                    {
                        url_str = "https://hs-music-player.herokuapp.com/getSongDataFromSpotify?query=" + t;

                }urlObj = new URL(url_str);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                JSONObject jsonObject = new JSONObject(line);
                int status = (int) jsonObject.get("status");
                if( status == 200 )
                {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        SongMetaDataObject s = new SongMetaDataObject(object.getString("track"), object.getString("album"), object.getString("artist"), object.getString("image"));
                        songList.add(s);
                    }
                }
                else
                    {
                        if(api == 1)
                            refreshToken();


                        postToast(line,SearchSongOnlineActivity.this,TOAST_NORMAL);
                }
            } catch (UnknownHostException e)
            {
                Log.d("!!!!!!!!!!hosr->", e.toString());
                urlConnection.disconnect();

            } catch (MalformedURLException e) {
                Log.d("!!!!!!!!!!mal->", e.toString());
                e.printStackTrace();

            } catch (IOException e)
            {
                Log.d("!!!!!!!!!!IO->", e.toString());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            SwipeRefreshLayout.setRefreshing(false);
            searchSongOnlineAdapter = new SearchSongOnlineAdapter(getApplicationContext(),songList);
            gridView.setAdapter(searchSongOnlineAdapter);
            if(songList.size() < 1)
            {
                NoDataFound.setVisibility(View.VISIBLE);
            }
            else
            {
                NoDataFound.setVisibility(View.GONE);
            }


        }
    }


    private void refreshToken()
    {
        try {
            String url = "https://hs-music-player.herokuapp.com/refresh";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            try {
                builder.setToolbarColor(ThemeUtils.getThemePrimaryColor(SearchSongOnlineActivity.this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(SearchSongOnlineActivity.this, Uri.parse(url));
        } catch (Exception e) {
            postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
            e.printStackTrace();
        }
    }

    private void search()
    {
        NoDataFound.setVisibility(View.GONE);

        int id = apiRadioGroup.getCheckedRadioButtonId();
        if(R.id.napsterRadio == id)
        {
            api = 2;
        }
        else
        {
            api = 1;
        }
        SwipeRefreshLayout.setRefreshing(true);
        searchText = Search.getText().toString();
        if(!searchText.equals(null) || !searchText.equals("")) {
            LoadData loadData = new LoadData();
            loadData.execute();
        }
    }


    public void setData(ArrayList<SongMetaDataObject> slist,int position)
    {
        songMetaDataObject = slist.get(position);
        progressDialog.show();
        LoadBitmap loadBitmap = new LoadBitmap();
        loadBitmap.execute();

    }

    private void editMetaData(final String... str) {
        AsyncTask.execute(() -> {
            String resultString;
            try
            {
                changeTagsBeforeWriting(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (f != null) {
                try {
                    AudioFileIO.write(f);
                    resultString = getString(R.string.successfully_edited_song_scanned);
                } catch (Exception e) {
                    e.printStackTrace();
                    resultString = getString(R.string.error_label);
                }
            } else {
                resultString = getString(R.string.error_label);
            }
            final String finalB = resultString;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalB.equals(getString(R.string.successfully_edited_song_scanned))) {
                    try {
                        scanMediaAfterEdited();
                        postToast(finalB, getApplicationContext(), TOAST_SUCCESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                {
                    if (Statics.getTreeUri(SearchSongOnlineActivity.this) != null) {

                        editSongOnExternalSDCard(songMetaDataObject.getTrackName(),songMetaDataObject.getAlbumName(),songMetaDataObject.getArtistName());

                    }
                }
            });
        });
        progressDialog.dismiss();
        Intent intent = new Intent(SearchSongOnlineActivity.this,EditMetaDataActivity.class);
        intent.putExtra("ID",ID);
        intent.putExtra("PATH",path);
        startActivity(intent);
        finish();
    }

    private void editSongOnExternalSDCard(final String... str) {
        AsyncTask.execute(() -> {
            String resultString = "";
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile documentFile = FileStatics.getDocumentFileIfAllowedToWrite(file, getApplicationContext());
                if (documentFile != null) {
                    File createdFile = FileUtils.copyFileToCacheSpace(file.getPath(), file.getName(), getApplicationContext());
                    if (createdFile != null && createdFile.exists()) {
                        try {
                            f = AudioFileIO.read(createdFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            changeTagsBeforeWriting(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            AudioFileIO.write(f);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultString = getString(R.string.error_label);
                        }
                        if (!resultString.equals(getString(R.string.error_label))) {
                            if (copyFileToExternalSDCardForGreaterThanKitkat(file, file.getParentFile(), createdFile.getPath(), createdFile.getName(), getApplicationContext())) {
                                audio = file;
                                createdFile.delete();
                                resultString = getString(R.string.successfully_edited_song_scanned);
                            } else {
                                resultString = getString(R.string.cant_edit_this_song);
                            }
                        }
                    } else {
                        resultString = getString(R.string.cant_edit_this_song);
                    }
                } else {
                    resultString = getString(R.string.cant_edit_this_song);
                }
            } else {
                MediaFile mediaFile = new MediaFile(getContentResolver(), file);
                if (mediaFile.getFile().exists()) {
                    File createdFile = FileUtils.copyFileToCacheSpace(file.getPath(), file.getName(), getApplicationContext());
                    if (createdFile != null && createdFile.exists()) {
                        try {
                            f = AudioFileIO.read(createdFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            changeTagsBeforeWriting(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            AudioFileIO.write(f);
                        } catch (Exception e) {
                            e.printStackTrace();
                            resultString = getString(R.string.error_label);
                        }
                        try {
                            mediaFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!resultString.equals(getString(R.string.error_label))) {
                            if (copyFileToExternalSDCardForKitkat(createdFile.getPath(), mediaFile)) {
                                audio = file;
                                createdFile.delete();
                                resultString = getString(R.string.successfully_edited_song_scanned);
                            } else {
                                resultString = getString(R.string.cant_edit_this_song);
                            }
                        }
                    } else {
                        resultString = getString(R.string.cant_edit_this_song);
                    }
                } else {
                    resultString = getString(R.string.cant_edit_this_song);
                }
            }
            final String finalB = resultString;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalB.equals(getString(R.string.successfully_edited_song_scanned))) {
                    try {
                        scanMediaAfterEdited();
                        postToast(finalB, getApplicationContext(), TOAST_SUCCESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    postToast(finalB, getApplicationContext(), TOAST_INFO);
                }
            });
        });
    }


    private void changeTagsBeforeWriting(String... str) {
        Tag newTag;
        newTag = f.getTag();
        if (newTag != null) {
            try {
                newTag.setField(FieldKey.TITLE, str[0]);
                newTag.setField(FieldKey.ALBUM, str[1]);
                newTag.setField(FieldKey.ARTIST, str[2]);
                String lyricsText = getLyricsFromCache(getApplicationContext(), ID);
                if( lyricsText.contains("No lyrics") ) {
                    newTag.setField(FieldKey.LYRICS, "");

                }
                else {
                    newTag.setField(FieldKey.LYRICS, lyricsText);
                }
                newTag.deleteArtworkField();
                newTag.addField(artwork);
                newTag.setField(artwork);
            } catch (Exception e) {
                e.printStackTrace();
            }
            f.setTag(newTag);
            //audio.setLastModified(createdDate.getTime());
        }
    }


    private void scanMediaAfterEdited() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("SymphonyLyrics", Context.MODE_PRIVATE);
        File myFile = new File(directory, ID + ".txt");
        if (myFile.exists()) {
            if (myFile.delete()) {
                Log.v("isDeleted", "yes");
            }
        }
        SingleMediaScanner(getApplicationContext(), audio);
    }

    public void SingleMediaScanner(Context context, File file) {
        this.file = file;
        mediaScannerConnection = new MediaScannerConnection(context, this);
        mediaScannerConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mediaScannerConnection.scanFile(file.getAbsolutePath(), null);
    }


    @Override
    public void onScanCompleted(String path, Uri uri) {
        mediaScannerConnection.disconnect();
    }


    public class LoadBitmap extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                URL url = new URL(songMetaDataObject.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                saveToInternalStorage(myBitmap,searchSongOnlineActivity);
            } catch (IOException e) {
                return null;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try
            {
                ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
                File directory = contextWrapper.getDir("SymphonyCache", Context.MODE_PRIVATE);
                File cacheFile = new File(directory, "cache.jpg");
                artwork = ArtworkFactory.createArtworkFromFile(cacheFile);
            }
            catch (Exception e) {
                postToast(R.string.cant_set_this_artwork, getApplicationContext(), TOAST_ERROR);
                e.printStackTrace();
            }
            editMetaData(songMetaDataObject.getTrackName(),songMetaDataObject.getAlbumName(),songMetaDataObject.getArtistName());

        }
    }





}
