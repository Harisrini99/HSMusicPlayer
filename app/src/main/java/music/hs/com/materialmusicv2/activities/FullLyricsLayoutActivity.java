package music.hs.com.materialmusicv2.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.chibde.visualizer.LineVisualizer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.Tree;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.hs.com.materialmusicv2.LineBarVisualizer;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.ShowDatabaseObject;
import music.hs.com.materialmusicv2.adapters.ShowDatabaseAdapter;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomSheetLyricsFragment;
import music.hs.com.materialmusicv2.customviews.nowplaying.NowPlayingLyrics;
import music.hs.com.materialmusicv2.customviews.others.CustomColorSwitchCompat;
import music.hs.com.materialmusicv2.customviews.others.SquareImageView;
import music.hs.com.materialmusicv2.lyrics.LRC;
import music.hs.com.materialmusicv2.lyrics.MagicFileChooser;
import music.hs.com.materialmusicv2.lyrics.flip;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.AlbumArt;
import music.hs.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.hs.com.materialmusicv2.objects.events.MediaSessionData;
import music.hs.com.materialmusicv2.objects.events.PlaybackPosition;
import music.hs.com.materialmusicv2.objects.events.PlaybackState;
import music.hs.com.materialmusicv2.objects.events.RepeatState;
import music.hs.com.materialmusicv2.objects.events.ShuffleState;
import music.hs.com.materialmusicv2.objects.events.controllerevents.VisualizerData;
import music.hs.com.materialmusicv2.service.MusicService;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setBackgroundColorFilter;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.hs.com.materialmusicv2.utils.controller.Controller.changeRepeat;
import static music.hs.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.hs.com.materialmusicv2.utils.controller.Controller.getCurrentSong;
import static music.hs.com.materialmusicv2.utils.controller.Controller.getSongPosition;
import static music.hs.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.hs.com.materialmusicv2.utils.controller.Controller.seekTo;
import static music.hs.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.hs.com.materialmusicv2.utils.drawableutils.DrawableUtils.getPlayPauseResourceBlack;
import static music.hs.com.materialmusicv2.utils.drawableutils.DrawableUtils.getRepeatResourceBlack;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.deleteLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.getRealString;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Statics.treeUri;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class FullLyricsLayoutActivity extends MusicPlayerActivity implements NowPlayingLyrics.ClickEventListener{

    @Nullable
    @BindView(R.id.albumArt)
    SquareImageView AlbumArt;
    @BindView(R.id.gradient)
    SquareImageView Gradient;
    @BindView(R.id.wholeLayout)
    RelativeLayout WholeLayout;
    @BindView(R.id.container)
    View container;
    @Nullable
    @BindView(R.id.lapsedTime2)
    TextView lapsedTime2;
    @Nullable
    @BindView(R.id.totalDuration)
    TextView totalDuration;
    @BindView(R.id.playPrevious)
    ImageButton playPrevious;
    @BindView(R.id.playPause)
    ExtendedFloatingActionButton playPause;
    @BindView(R.id.playNext)
    ImageButton playNext;
    @BindView(R.id.shuffle)
    ImageButton shuffle;
    @BindView(R.id.repeat)
    ImageButton repeat;
    @Nullable
    public static SeekBar songProgress2;
    @BindView(R.id.songName)
    MaterialButton SongName;
    @BindView(R.id.back_image)
    ImageButton BackImage;
    @BindView(R.id.noLyricsText)
    TextView NoLyricsText;
    @BindView(R.id.setLyrics)
    TextView SetLyrics;
    @BindView(R.id.noLyricsLayout)
    LinearLayout NoLyricslayout;
    @BindView(R.id.lyricsLayout)
    LinearLayout Lyricslayout;
    @BindView(R.id.visualizer)
    LineBarVisualizer Visualizer;
    @BindView(R.id.switchLyrics)
    CustomColorSwitchCompat SwitchLyrics;
    @BindView(R.id.lyricsLayoutScroll)
    LinearLayout LyricslayoutScroll;





    @OnClick({ R.id.playPause, R.id.playPrevious, R.id.playNext, R.id.repeat, R.id.shuffle, R.id.back_image,R.id.setLyrics})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPause: {
                pauseOrResumePlayer();
                break;
            }
            case R.id.playPrevious: {
                playPrevious();
                break;
            }
            case R.id.playNext: {

                playNext();
                break;
            }
            case R.id.repeat: {
                changeRepeat();
                break;
            }
            case R.id.shuffle: {
                changeShuffle();
                break;
            }
            case R.id.back_image: {
                onBackPressed();
                break;
            }
            case R.id.setLyrics:
            {
                showLyrics();
                break;
            }
        }
    }



    private boolean wasPlaying = false;
    private boolean isUserTouchingSeekBar = false;
    int contrast;
    int foreGroundColor;
    int backGroundColor;
    Timer timer;
    TextView[] lyricText;
    TextView[] lyricTextScroll;
    TreeMap<Integer,String> list;
    int currentPosition = 0;
    int lastPosition = 0;
    private BottomSheetLyricsFragment bottomSheetLyricsFragment;
    private NowPlayingLyrics nowPlayingLyrics;
    File fileLRC;
    LRC lrc;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_lyrics_layout);

        setTheme(ThemeUtils.getTheme(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        songProgress2 = (SeekBar)findViewById(R.id.songProgress2);

        ButterKnife.bind(this);
        list = new TreeMap<>();
        setUpSeekBar();
       timer = new Timer(true);
        timer.schedule(timerTask, 0, 300);
        createTextViews();
        createTextViewsScroll();



    }


    private void setUpSeekBar() {
        if (songProgress2 != null) {
            songProgress2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isUserTouchingSeekBar = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isUserTouchingSeekBar = false;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();


        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        if (wasPlaying) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (data == null) return;
        if (requestCode == 2) {
            Uri uri = data.getData();
            fileLRC = new File(MagicFileChooser.getAbsolutePathFromUri(this, uri));
            try {
                lrc = flip.Parse(fileLRC);
                Song song = getCurrentSong();
                String temp = lrc.getlyrics();

                StringBuilder sb = new StringBuilder(temp);
                sb.deleteCharAt(temp.indexOf("{"));
                sb.deleteCharAt(temp.indexOf("}") - 1);
                deleteLyricsFromCache(getApplicationContext(),song.getId());
                LyricsUtils.deleteLyricsFromCache(getApplicationContext(),song.getId());
                LyricsUtils.setLyricsInCache(getApplicationContext(), sb.toString(), song.getId());
                setLyrics(sb.toString());
                bottomSheetLyricsFragment.dismiss();
                promptUserToUpoloadLyrics();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 0) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 42) {
            treeUri = data.getData();
            final SharedPreferences.Editor editor = getSharedPreferences("mypref", MODE_PRIVATE).edit();
            editor.putString("treeUri", treeUri.toString());
            editor.apply();
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumArt albumArt) {
        Bitmap bitmap = albumArt.albumArt;

        AlbumArt.setImageBitmap(bitmap);
        setBackgroundColorFilter(albumArt.backgroundColor, Gradient);
        WholeLayout.setBackgroundColor(albumArt.backgroundColor);
        contrast = ContrastColor(albumArt.backgroundColor);
        foreGroundColor = albumArt.foregroundColor;
        backGroundColor = albumArt.backgroundColor;
        setColors(albumArt.backgroundColor, foreGroundColor);
        String lyricsText = LyricsUtils.getLyrics(HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().get(getSongPosition()).getPath(), getApplicationContext(), getCurrentSong().getId());
        setLyrics(lyricsText);


    }





    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState) {
        setPlayPause(playbackState.state);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition) {
        setSongProgress(playbackPosition.position, playbackPosition.duration);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RepeatState repeatState) {
        setRepeat(repeatState.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ShuffleState shuffleState) {
        setShuffle(shuffleState.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong)
    {
        ArrayList<Song> songs = QueryUtils.getAllSongs(getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
        ArrayList<Song> t = QueryUtils.getLastPlayedSongs(this);
        SongName.setText(currentPlayingSong.songName);
        Visualizer.setPlayer(HSMusicApplication.getInstance().getMediaSessionUtils().getAudioID());
    }


    private void setSongProgress(int progress, int maxProgress) {
        final String lapsedText = covertMilisToTimeString(progress);
        final String durationText = covertMilisToTimeString(maxProgress);

        if (lapsedTime2 != null) {
            lapsedTime2.setText(lapsedText);
        }
        if (totalDuration != null) {
            totalDuration.setText(durationText);
        }
        if (isUserTouchingSeekBar) {
            return;
        }
        if (songProgress2 != null) {
            if (songProgress2.getMax() != maxProgress) {
                songProgress2.setMax(maxProgress);
            }
            songProgress2.setProgress(progress);
        }
    }

    private void setShuffle(boolean state) {
        if (shuffle != null) {
            if (state) {
                shuffle.setImageAlpha(255);
            } else {
                shuffle.setImageAlpha(80);
            }
        }
    }

    private void setRepeat(int state) {
        if (repeat != null) {
            repeat.setImageResource(getRepeatResourceBlack(state));
            if (state != 0) {
                repeat.setImageAlpha(255);
            } else {
                repeat.setImageAlpha(80);
            }
        }
    }

    private void setPlayPause(boolean isPlaying) {
        if (playPause != null)
        {
            playPause.setIconResource(getPlayPauseResourceBlack(isPlaying));
        }
        wasPlaying = isPlaying;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setColors(int backgroundColor, int foregroundColor)
    {


        songProgress2.setProgressBackgroundTintList(ColorStateList.valueOf(contrast));
        songProgress2.setProgressTintList(ColorStateList.valueOf(foregroundColor));
        songProgress2.getThumb().setColorFilter(contrast, PorterDuff.Mode.SRC_IN);
        setColorFilter(contrast,  shuffle,playPrevious, playNext, repeat);

        setColorFilter(foregroundColor,BackImage);
        setBackgroundColorFilter(foregroundColor,BackImage);



        Drawable checked = getResources().getDrawable(R.drawable.ic_toggle_checked);
        Drawable unChecked = getResources().getDrawable(R.drawable.ic_toggle_unchecked);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;

        checked.setColorFilter(foregroundColor,mMode);
        unChecked.setColorFilter(foregroundColor,mMode);

        if(SwitchLyrics.isChecked())
            SwitchLyrics.setTrackDrawable(checked);
        else
            SwitchLyrics.setTrackDrawable(unChecked);

        SwitchLyrics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    LyricslayoutScroll.setVisibility(View.VISIBLE);
                    Lyricslayout.setVisibility(View.GONE);
                    SwitchLyrics.setTrackDrawable(checked);
                }
                else
                    {

                        LyricslayoutScroll.setVisibility(View.GONE);
                        Lyricslayout.setVisibility(View.VISIBLE);
                    SwitchLyrics.setTrackDrawable(unChecked);
                }
            }
        });

        SwitchLyrics.setBgOffColor(foregroundColor);
        SwitchLyrics.setBgOnColor(foregroundColor);
        SwitchLyrics.setToggleOnColor(foregroundColor);
        SwitchLyrics.setToggleOffColor(foregroundColor);





        SongName.setTextColor(ContrastColor(foregroundColor));
        SongName.setBackgroundColor(foregroundColor);
        setTextColor(contrast, lapsedTime2, totalDuration);
        NoLyricsText.setTextColor(contrast);
        SetLyrics.setTextColor(foregroundColor);

        if (nowPlayingLyrics != null) {
            nowPlayingLyrics.setColors(backgroundColor, foregroundColor);
        }


        if (playPause != null) {
            playPause.setIconTint(ColorStateList.valueOf(ContrastColor(foregroundColor)));
            playPause.setBackgroundColor(foregroundColor);
            playPause.setRippleColor(ColorStateList.valueOf(backgroundColor));
        }



    }

    protected TreeMap<Integer,String> convertToTreeMap(String text){
        TreeMap<Integer,String> data = new TreeMap<>();
        String[] split = text.split(",");
        for ( int i=0; i <split.length; i++ )
        {
            String t[] = split[i].split("=");
            t[0] = t[0].replaceAll("\\s+","");

            if( t.length > 1)
                data.put(Integer.parseInt(t[0]),t[1]);
        }
        return data;
    }

    private void update(TreeMap<Integer,String> list,int millisecond,boolean isAnimation)
    {
        Animation aniSlide = AnimationUtils.loadAnimation(this, R.anim.linear_bottom_top);
        int j=4;
        int first =(contrast & 0x00FFFFFF) | 0x90000000;
        int second =(contrast & 0x00FFFFFF) | 0x80000000;
        int third =(contrast & 0x00FFFFFF) | 0x70000000;
        int forth =(contrast & 0x00FFFFFF) | 0x60000000;

        Typeface t = getResources().getFont(R.font.encode_sans_narrow);
        for(int i=8;i>=0&&j>=-4;i--,j--)
        {
            lyricText[i].setText(getLine(millisecond,j));
            if(i==8 || i==0)
                lyricText[i].setTextColor(forth);
            else if(i==7 || i==1)
                lyricText[i].setTextColor(third);
            else if(i==6 || i==2)
                lyricText[i].setTextColor(second);
            else if(i==5 || i==3)
                lyricText[i].setTextColor(first);
            lyricText[i].setTypeface(t);

        }
        lyricText[4].setTextSize(17);
        t = getResources().getFont(R.font.encode_sans_narrow_medium);
        lyricText[4].setTypeface(t);
        lyricText[4].setTextColor(foreGroundColor);

    }

    private void createTextViews()
    {
        lyricText = new TextView[9];
        for(int i=8;i>=0;i--)
        {
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.full_lyrics_format, null);
            lyricText[i] = (TextView) v.findViewById(R.id.lyric);
            lyricText[i].setTextColor(contrast);
            Lyricslayout.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }

    }



    private void createTextViewsScroll()
    {
        int n = list.size();
        lyricTextScroll = new TextView[n];
        Typeface tf = getResources().getFont(R.font.encode_sans_narrow);
        for(int i=n-1;i>=0;i--)
        {
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.full_lyrics_format, null);
            lyricTextScroll[i] = (TextView) v.findViewById(R.id.lyric);
            int normal =(contrast & 0x00FFFFFF) | 0x70000000;
            lyricTextScroll[i].setTextColor(normal);
            Set<Integer> keys = list.keySet();
            Integer i_key = getValueFromIndex(keys,i);
            lyricTextScroll[i].setText(list.get(i_key));
            lyricTextScroll[i].setTypeface(tf);
            LyricslayoutScroll.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }

    }



    private Integer getValueFromIndex(Set<Integer> set, int pos) {
        Iterator<Integer> itr = set.iterator();
        Integer value=null;
        for(int i = 0; itr.hasNext(); i++)
        {
            value = itr.next();
            if (i == pos)
            {
                break;
            }
        }
        return value;
    }

    private int getIndexFromValue(Set<Integer> set, Integer pos) {
        Iterator<Integer> itr = set.iterator();
        int value=0;
        for(int i = 0; itr.hasNext(); i++)
        {
            value = i;
            Integer t = itr.next();
            if (t.equals(pos))
            {
                break;
            }
        }
        return value;
    }




    private TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {

            Message msg = new Message();
            msg.what = songProgress2.getProgress();
            handler.sendMessage(msg);
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Integer t = getLineIndex(msg.what);
            Set<Integer> keys = list.keySet();
            Integer first = getValueFromIndex(keys,0);
            currentPosition = getIndexFromValue(keys,t);
            if ((list != null && !list.isEmpty()) && currentPosition != lastPosition && msg.what >= first)
            {
                update(list,msg.what,true);

                if(lyricTextScroll.length > 0)
                {
                    Typeface typeNormal = getResources().getFont(R.font.encode_sans_narrow);
                    Typeface typeSelected = getResources().getFont(R.font.encode_sans_narrow_medium);
                    lyricTextScroll[lastPosition].setTypeface(typeNormal);
                    lyricTextScroll[currentPosition].setTypeface(typeSelected);
                    int normal =(contrast & 0x00FFFFFF) | 0x80000000;
                    lyricTextScroll[lastPosition].setTextColor(normal);
                    lyricTextScroll[currentPosition].setTextColor(foreGroundColor);
                }
                lastPosition = currentPosition;
            }
            if((list != null && !list.isEmpty()) && msg.what < first)
            {
                int temp = getValueFromIndex(keys,0) + 1;
                update(list,temp,false);
            }
        }
    };

    private Integer getLineIndex(int millisecond){
        if (list.isEmpty()) return null;
        Vector<Integer> time = new Vector<>(list.keySet());
        int LinePtr = 0;
        for (; LinePtr < time.size(); LinePtr++)
        {

            if (LinePtr + 1 >= time.size())
            {
                break;
            }
            if ((millisecond > time.get(LinePtr)) && (millisecond < time.get(LinePtr + 1))) break;
        }
        return time.get(LinePtr);
    }

    public String getLine(int millisecond, int offset){
        if (list.isEmpty()) return null;
        Vector<Integer> time = new Vector<>(list.keySet());
        int LinePtr = 0;
        for (; LinePtr < time.size(); LinePtr++){
            if (LinePtr + 1 >= time.size()) break;
            if ((millisecond > time.get(LinePtr)) && (millisecond < time.get(LinePtr + 1))) break;
        }
        if (LinePtr + offset < 0) return "";
        if (LinePtr + offset >= time.size()) return "";
        return list.get(time.get(LinePtr + offset));
    }

    private void showLyrics() {
        if (bottomSheetLyricsFragment == null) {
            bottomSheetLyricsFragment = new BottomSheetLyricsFragment();
            bottomSheetLyricsFragment.setCallback(nowPlayingLyrics -> {
                if (nowPlayingLyrics != null)
                {
                    this.nowPlayingLyrics = nowPlayingLyrics;
                    nowPlayingLyrics.setInvisible();
                    this.nowPlayingLyrics.setClickEventListener(this);
                    //setLyrics();
                    AlbumArt albumArt = EventBus.getDefault().getStickyEvent(AlbumArt.class);
                    this.nowPlayingLyrics.setColors(albumArt.backgroundColor, albumArt.foregroundColor);
                }
            });
        }
        bottomSheetLyricsFragment.show(getSupportFragmentManager(), bottomSheetLyricsFragment.tag);
    }

    @Override
    public void onSearchClicked() {
        Song song = getCurrentSong();
        String track = getRealString(song.getName());
        String artist = getRealString(song.getArtist());
        try {
            String url = "https://www.google.com/search?q=" + track + URLEncoder.encode(" ", "UTF-8") + ".lrc";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            try {
                builder.setToolbarColor(ThemeUtils.getThemePrimaryColor(FullLyricsLayoutActivity.this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(FullLyricsLayoutActivity.this, Uri.parse(url));
        } catch (Exception e) {
            postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
            e.printStackTrace();
        }
    }


    @Override
    public void onDeleteClicked()
    {
    }

    @Override
    public void onSearchDatabaseClicked() {
        bottomSheetLyricsFragment.dismiss();
        Song song = getCurrentSong();
        Intent intent = new Intent(FullLyricsLayoutActivity.this,ShowDatabaseActivity.class);
        startActivity(intent);
    }


    @Override
    public void onAddClicked() {
        Song song = getCurrentSong();
        openFileIntent("*/*", 2);
    }

    private void setLyrics(String lyricsText)
    {
        lastPosition = 0;
        currentPosition = 0;
        list = convertToTreeMap(lyricsText);
        if(LyricslayoutScroll != null)
            LyricslayoutScroll.removeAllViews();

        if(list != null && !list.isEmpty())
        {
            createTextViewsScroll();
            Set<Integer> keys = list.keySet();
            int temp = getValueFromIndex(keys,0) + 1;
            update(list,temp,false);
            NoLyricslayout.setVisibility(View.GONE);
            if(SwitchLyrics.isChecked())
            {
                Lyricslayout.setVisibility(View.GONE);
                LyricslayoutScroll.setVisibility(View.VISIBLE);
            }
            else
            {
                Lyricslayout.setVisibility(View.VISIBLE);
                LyricslayoutScroll.setVisibility(View.GONE);
            }
            Visualizer.setVisibility(View.GONE);
            SwitchLyrics.setVisibility(View.VISIBLE);
        }
        else
        {
            NoLyricsText.setText("Loading Lyrics....");
            Song cuurentSong = getCurrentSong();
            loadLyricsFromDatabase(cuurentSong.getName());
            //Visualizer Settings
            Visualizer.setVisibility(View.VISIBLE);
            Visualizer.setColor(foreGroundColor);
            Visualizer.setDensity(70);
            Visualizer.setMiddleLineColor(foreGroundColor);




            NoLyricslayout.setVisibility(View.VISIBLE);
            Lyricslayout.setVisibility(View.GONE);
            LyricslayoutScroll.setVisibility(View.GONE);
            SwitchLyrics.setVisibility(View.GONE);
        }
    }

    private void openFileIntent(String MIME, int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MIME);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, ""), requestCode);
    }


    private void loadLyricsFromDatabase(String Name)
    {
        final String songName = Name.toLowerCase();
        DatabaseReference imagesQuery = FirebaseDatabase.getInstance().getReference().child("Song Details");
        imagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flag =0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot1.getValue();
                    String nameFromDatabase = map.get("Name");
                    nameFromDatabase = nameFromDatabase.toLowerCase();
                    if( nameFromDatabase.contains(songName) )
                    {
                        String lyrics = map.get("Lyrics");
                        list = convertToTreeMap(lyrics);
                        setLyrics(lyrics);
                        flag =1;
                        break;
                    }
                }
                if(flag == 0)
                    NoLyricsText.setText("No Lyrics Found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void promptUserToUpoloadLyrics()
    {
        int colorAccent = ThemeUtils.getThemeAccentColor(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String titleText = "New Song Lyrics Added!!";
        String messageText = "Do you want to Upload This Lyrics To Database?";

        builder.setTitle(messageText);
        Drawable d = getResources().getDrawable(R.drawable.ic_refresh_icon);
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, colorAccent);
        builder.setIcon(wrappedDrawable);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadLyrics();
            }
        });
        builder.setNegativeButton("No",null);
        AlertDialog dialog = builder.create();
        dialog.show();


        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.WHITE);
        negativeButton.setBackgroundColor(Color.TRANSPARENT);


        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(colorAccent);
        positiveButton.setBackgroundColor(Color.TRANSPARENT);

    }


    private void uploadLyrics()
    {
        int colorAccent = ThemeUtils.getThemeAccentColor(this);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please Wait.....");
        progressDialog.show();
        FirebaseDatabase f = FirebaseDatabase.getInstance();
        DatabaseReference d = f.getReference().child("Song Details");
        ArrayList<Song> songs = QueryUtils.getAllSongs(getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
        HashMap<String,String> map = new HashMap<>();
        for(Song s : songs)
        {

            String lyricsText = getLyricsFromCache(getApplicationContext(),s.getId());
            map.put("Name",s.getName());
            map.put("Artist",s.getArtist());
            map.put("Duration",String.valueOf(s.getDuration()));
            map.put("Lyrics",lyricsText);
            if(!lyricsText.equals( "No lyrics"))
                d.child(String.valueOf(s.getId())).setValue(map);
        }
        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Song Details Uploaded");
        builder.setPositiveButton("ok",null);
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(colorAccent);
        positiveButton.setBackgroundColor(Color.TRANSPARENT);


    }


}