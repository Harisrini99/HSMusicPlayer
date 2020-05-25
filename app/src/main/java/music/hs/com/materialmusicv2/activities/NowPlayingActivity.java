package music.hs.com.materialmusicv2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.blurry.Blurry;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomNowPlayingMenu;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomSheetLyricsFragment;
import music.hs.com.materialmusicv2.customviews.nowplaying.NowPlayingBackground;
import music.hs.com.materialmusicv2.customviews.nowplaying.NowPlayingLyrics;
import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.hs.com.materialmusicv2.customviews.others.SquareImageView;
import music.hs.com.materialmusicv2.lyrics.LRC;
import music.hs.com.materialmusicv2.lyrics.MagicFileChooser;
import music.hs.com.materialmusicv2.lyrics.flip;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.AlbumArt;
import music.hs.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.hs.com.materialmusicv2.objects.events.FavoriteChanged;
import music.hs.com.materialmusicv2.objects.events.PlaybackPosition;
import music.hs.com.materialmusicv2.objects.events.PlaybackState;
import music.hs.com.materialmusicv2.objects.events.RepeatState;
import music.hs.com.materialmusicv2.objects.events.ShuffleState;
import music.hs.com.materialmusicv2.objects.events.SongListChanged;
import music.hs.com.materialmusicv2.objects.events.SongPositionInQueue;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView.CORNER_ALL;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.hs.com.materialmusicv2.utils.controller.Controller.addCurrentSongToFavorite;
import static music.hs.com.materialmusicv2.utils.controller.Controller.changeRepeat;
import static music.hs.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.hs.com.materialmusicv2.utils.controller.Controller.getCurrentSong;
import static music.hs.com.materialmusicv2.utils.controller.Controller.getSongPosition;
import static music.hs.com.materialmusicv2.utils.controller.Controller.isCurrentSongFavorite;
import static music.hs.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playAtSongPosition;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.hs.com.materialmusicv2.utils.controller.Controller.removeCurrentSongFromFavorite;
import static music.hs.com.materialmusicv2.utils.controller.Controller.seekTo;
import static music.hs.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.hs.com.materialmusicv2.utils.drawableutils.DrawableUtils.getPlayPauseResourceBlack;
import static music.hs.com.materialmusicv2.utils.drawableutils.DrawableUtils.getRepeatResourceBlack;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.deleteLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.dipToPixels;
import static music.hs.com.materialmusicv2.utils.misc.Etc.getRealString;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Statics.treeUri;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.hs.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class NowPlayingActivity extends MusicPlayerActivity implements SharedPreferences.OnSharedPreferenceChangeListener, NowPlayingBackground.ScrollListener,NowPlayingLyrics.ClickEventListener {

        @Nullable
        @BindView(R.id.albumArt)
        SquareImageView AlbumArt;
        @BindView(R.id.lyricsButton)
        ImageButton LyricButton;

        @Nullable
        @BindView(R.id.static_noRotate)
        NowPlayingBackground Static_noRotate;
        @BindView(R.id.container)
        View container;
        @BindView(R.id.songName)
        TextView songName;
        @Nullable
        @BindView(R.id.artistNameAndAlbumName)
        TextView artistNameAndAlbumName;
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
        @BindView(R.id.menu)
        ImageButton menu;
        @BindView(R.id.repeat)
        ImageButton repeat;
        @BindView(R.id.favoriteButton)
        ImageButton favoriteButton;
        @BindView(R.id.addPlaylist)
        ImageButton AddPlaylist;
        @BindView(R.id.equiliser)
        ImageButton Equiliser;
        @Nullable
        @BindView(R.id.songProgress)
        CircularSeekBar songProgress;
        @Nullable
        @BindView(R.id.songProgress2)
        SeekBar songProgress2;

        @BindView(R.id.back_image)
        ImageButton BackImage;
        @BindView(R.id.bg)
        ImageView Bg;
        @BindView(R.id.nowplaying_toolbar)
        Toolbar toolbar;
        @BindView(R.id.nowPlayingToolbar)
        TextView NowPlayingToolbar;

        @BindView(R.id.textView_LRC_0)
        TextView LineB1;
        @BindView(R.id.textView_LRC_1)
        TextView Line00;
        @BindView(R.id.textView_LRC_2)
        TextView Line01;


    @OnClick({R.id.lyricsButton, R.id.playPause, R.id.playPrevious, R.id.playNext, R.id.repeat, R.id.shuffle, R.id.favoriteButton, R.id.menu, R.id.back_image, R.id.equiliser, R.id.addPlaylist})
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
            case R.id.favoriteButton: {
                createPlaylist("Favorites", getApplicationContext());
                if (isCurrentSongFavorite(getApplicationContext())) {
                    removeCurrentSongFromFavorite(getApplicationContext());
                } else {
                    addCurrentSongToFavorite(getApplicationContext());
                }
                break;
            }

            case R.id.back_image: {
                onBackPressed();
                break;
            }
            case R.id.equiliser: {
                Intent equalizerIntent = new Intent(NowPlayingActivity.this, EqualizerActivity.class);
                startActivity(equalizerIntent);
                break;
            }
            case R.id.addPlaylist: {
                Song song = getCurrentSong();
                showAddToPlaylistDialog(NowPlayingActivity.this, song, false);
                break;
            }
            case R.id.lyricsButton: {
                //showLyrics();
                showLyrics();
                break;
            }
            case R.id.menu: {
                if (menu == null) {
                    return;
                }


               /* PopupMenu popupMenu = new PopupMenu(menu.getContext(), menu);
                popupMenu.getMenuInflater().inflate(R.menu.menu_now_playing_song, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    Song song = getCurrentSong();
                    switch (item.getItemId()) {
                        case R.id.action_add_to_playlist: {
                            showAddToPlaylistDialog(NowPlayingActivity.this, song, false);
                            break;
                        }
                        case R.id.action_share: {
                            shareSong(NowPlayingActivity.this, song.getPath());
                            break;
                        }

                        case R.id.action_edit: {
                            Intent intent = new Intent(NowPlayingActivity.this, EditMetaDataActivity.class);
                            intent.putExtra("PATH", getCurrentSong().getPath());
                            intent.putExtra("ID", getCurrentSong().getId());
                            intent.putExtra("AlBUM_ID", getCurrentSong().getAlbumId());
                            startActivity(intent);
                            break;
                        }


                        case R.id.action_set_as_ringtone: {
                            setAudioAsRingtone(new File(song.getPath()), getApplicationContext());
                            break;
                        }
                    }
                    return true;
                });
                popupMenu.show();*/
                openNavDrawer();
                break;
            }
        }
    }


    public ObjectAnimator rt;
    TextView hsongName;
    TextView hartistName;
    RoundedSquareImageView halbumArt;
    private BottomSheetLyricsFragment bottomSheetLyricsFragment;
    private NowPlayingLyrics nowPlayingLyrics;
    private Bitmap tempAlbum;
    private CurrentPlayingSong tempSong;
   TreeMap<Integer, String> Lines;
    private boolean wasPlaying = false;
    private boolean hasRotationStarted = false;
    private BottomNowPlayingMenu bottomNowPlayingMenu;
    private boolean isUserTouchingSeekBar = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    File fileLRC;
    private Timer timer;
    LRC lrc;
    boolean isPause = false;
    int lrc_p = 0;
    static String lyricsText;
    private ValueAnimator colorAnimation;
    private int previousBackgroundColor = Color.BLACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean t = HSMusicApplication.getInstance().getPreferenceUtils().getIsCircleImage();
        setContentView(R.layout.temp);

        setTheme(ThemeUtils.getTheme(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        ButterKnife.bind(this);
        if (t) {
            setUpRotation();
        }
        setUpNowPlayingBackground();
        setUpCircularSeekBar();
        setUpSeekBar();


        timer = new Timer(true);
        timer.schedule(timerTask, 0, 300);

        Song song = getCurrentSong();
        if (song != null) {
            lyricsText = getLyricsFromCache(getApplicationContext(),song.getId());
           if(!lyricsText.equals("No lyrics") && lyricsText != "")
                Lines = convertToTreeMap(lyricsText);

        }

    }

    private void setUpNowPlayingBackground() {
        if (Static_noRotate != null) {
            Static_noRotate.setScrollListener(this);
        }
    }

    private void setUpCircularSeekBar() {
        if (songProgress != null) {
            songProgress.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                    if (fromUser) {
                        seekTo((int) progress);
                        Log.d("_____________________",progress+"");
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {
                    isUserTouchingSeekBar = false;
                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {
                    isUserTouchingSeekBar = true;
                }
            });
        }
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
        Song song = getCurrentSong();
        if (song != null) {
            lyricsText = getLyricsFromCache(getApplicationContext(),song.getId());
            if(!lyricsText.equals("No lyrics") && lyricsText != "")
                Lines = convertToTreeMap(lyricsText);

        }

        if (wasPlaying) {
        }
    }

    @Override
    public void onDestroy() {
        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();

        if (colorAnimation != null) {
            colorAnimation.cancel();
            colorAnimation.removeAllUpdateListeners();
            colorAnimation.removeAllListeners();
        }

        if (rt != null) {
            rt.cancel();
            rt.removeAllListeners();
            rt = null;
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
                setLyrics();
//                lrc.Show_C(10);
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

        tempAlbum = bitmap;
        setAlbumArt(bitmap);
        Blurry.with(this)
                .sampling(25)
                .from(albumArt.albumArt)
                .into(Bg);
        //  container.setBackground(d);
        //changeBG


        //if (HSMusicApplication.getInstance().getPreferenceUtils().getColorizeElementsAccordingToAlbumArt())
        {
          //  setColors(albumArt.backgroundColor, albumArt.foregroundColor);
        } //else {
            setColors(ThemeUtils.getThemePrimaryColor(NowPlayingActivity.this), ThemeUtils.getThemeAccentColor(NowPlayingActivity.this));

    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongPositionInQueue songPositionInQueue) {
        updateFavoriteButton();
        scrollTo(songPositionInQueue.position);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EqualizerActivity updateEqualizerActivity) {
        int colorAccent = ThemeUtils.getThemeAccentColor(this);
        boolean t = HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerEnabled();
        if(t)
        {
            setColorFilter(colorAccent,Equiliser);
        }
        else
        {
            setColorFilter(Color.WHITE,Equiliser);
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongListChanged songListChanged) {
        updateQueue(songListChanged);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState) {
        setPlayPause(playbackState.state);
        /*if(playbackState.state) {
            try {
                mediaPlayer.start();
                mediaPlayer.setDataSource(fileAudio.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {

            }
        }
        else
        {
            mediaPlayer.pause();
        }
        lrc_p = 0;*/


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
        tempSong = currentPlayingSong;
        setSongName(currentPlayingSong.songName);
        setArtistNameAndAlbumName(currentPlayingSong.artistName, currentPlayingSong.albumName);
        LineB1.setText("");
        Line00.setText("Loading Lyrics.....");
        Line01.setText("");
        Lines = null;
        lyricsText = LyricsUtils.getLyrics(HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().get(getSongPosition()).getPath(), getApplicationContext(), getCurrentSong().getId());
        if(!lyricsText.equals("No lyrics") && lyricsText != "")
        {
            Lines = new TreeMap<>();
            Lines = convertToTreeMap(lyricsText);
        }
        else
        {
            loadLyricsFromDatabase(currentPlayingSong.songName);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FavoriteChanged favoriteChanged) {
        setFavorite(favoriteChanged.favorite);
    }


    private void setArtistNameAndAlbumName(String artistName, String albumName) {
        if (artistNameAndAlbumName != null) {
            artistNameAndAlbumName.setText(String.format("%s • %s", artistName, albumName));
            artistNameAndAlbumName.setSelected(true);
        }
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
        if (songProgress != null) {
            if (songProgress.getMax() != maxProgress) {
                songProgress.setMax(maxProgress);
            }
            songProgress.setProgress(progress);
        }
        if (songProgress2 != null) {
            if (songProgress2.getMax() != maxProgress) {
                songProgress2.setMax(maxProgress);
            }
            songProgress2.setProgress(progress);
        }
    }

    private void setSongName(String name) {
        if (songName != null) {
            songName.setText(name);
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
        if (rt != null)
        {
            if (isPlaying)
            {
                if (!hasRotationStarted)
                {
                    rt.start();
                    hasRotationStarted = true;
                }
                rt.resume();
            } else
            {
                rt.pause();
            }
        }
        wasPlaying = isPlaying;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "nowPlayingStyle":
            case "visualizerBars":
            case "enableVisualizer":
            case "visualizerType":
            case "visualizerHeight":
            case "visualizerAnimationFrameRate":
                recreate();
                break;
        }
    }

    public void setAlbumArt(Bitmap albumArt) {

        {
            final Drawable roundedAlbumArt = new BitmapDrawable(getResources(),albumArt);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setColors(int backgroundColor, int foregroundColor)
    {

        {
            if (ContrastColor(getThemeWindowBackgroundColor(NowPlayingActivity.this)) != ContrastColor(backgroundColor)) {
                foregroundColor = backgroundColor;
            }
            backgroundColor = getThemeWindowBackgroundColor(NowPlayingActivity.this);
        }
        //animateColors(backgroundColor);
        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(NowPlayingActivity.this, ContrastColor(backgroundColor) == Color.BLACK);
        }

        if (songProgress != null) {
            songProgress.setCircleStrokeWidth(dipToPixels(NowPlayingActivity.this, 4));
            songProgress.setCircleProgressColor(foregroundColor);
            songProgress.setCircleColor(adjustAlpha(foregroundColor, 0.2f));
            songProgress.setPointerStrokeWidth(dipToPixels(NowPlayingActivity.this, 6));
            songProgress.setPointerColor(Color.WHITE);
            songProgress.setPointerHaloColor(foregroundColor);
        }
        int windowBackground = ThemeUtils.getThemeWindowBackgroundColor(NowPlayingActivity.this);
        int tint;
        if (ContrastColor(windowBackground) == ContrastColor(backgroundColor)) {
            tint = foregroundColor;
        } else {
            tint = backgroundColor;
        }
        if (nowPlayingLyrics != null) {
            nowPlayingLyrics.setColors(backgroundColor, foregroundColor);
        }

        int colorAccent = getThemeAccentColor(this);
//        setColorFilter(colorAccent, songProgress2);
        songProgress2.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        songProgress2.setProgressTintList(ColorStateList.valueOf(colorAccent));
        songProgress2.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        setColorFilter(backgroundColor,BackImage,menu);
        boolean t = HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerEnabled();
        if(t)
        {
            setColorFilter(colorAccent,Equiliser);
        }
        else
        {
            setColorFilter(Color.WHITE,Equiliser);
        }
        setColorFilter(Color.WHITE,  shuffle,playPrevious, playNext, repeat,LyricButton);
        setColorFilter(Color.WHITE,BackImage,menu);
        setTextColor(Color.WHITE, lapsedTime2, totalDuration, songName);
        Line00.setTextColor(colorAccent);
        if (playPause != null) {
            playPause.setIconTint(ColorStateList.valueOf(backgroundColor));
            playPause.setBackgroundColor(colorAccent);
            playPause.setRippleColor(ColorStateList.valueOf(backgroundColor));


        }
       // toolbar.setBackgroundColor(colorAccent);
        NowPlayingToolbar.setTextColor(Color.WHITE);


    }

    private void updateFavoriteButton() {
        if (executorService == null) {
            return;
        }
        executorService.execute(() -> {
            final boolean favorite = isCurrentSongFavorite(getApplicationContext());
            new Handler(Looper.getMainLooper()).post(() -> setFavorite(favorite));
        });
    }

    private void setFavorite(boolean favorite) {
        if (favoriteButton != null) {
            if (favorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }


    private void setUpRotation()
    {
//        View itemView = LayoutInflater.from(NowPlayingBackgroundAdapter.con.getContext()).inflate(R.layout.now_playing_cricle, NowPlayingBackgroundAdapter.con, false);
  //      CircleImageView lbumArt = itemView.findViewById(R.id.albumArt);
        rt = ObjectAnimator.ofFloat(Static_noRotate, "rotation", 0, 359);
        {
            rt.setDuration(15000);
            rt.setRepeatCount(ObjectAnimator.INFINITE);
            rt.setRepeatMode(ObjectAnimator.RESTART);
            rt.setInterpolator(new LinearInterpolator());

            rt.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    animation.setInterpolator(new LinearInterpolator());
                }
            });
        }
        rt.start();

    }

    private void updateQueue(@NonNull SongListChanged songListChanged)
    {
        if (songListChanged.songs != null && Static_noRotate != null) {
            Static_noRotate.setSongs(songListChanged.songs);
            Static_noRotate.setPreviousPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition());
            Static_noRotate.scrollTo(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition());
        }
    }

    public void scrollTo(final int position)
    {
        if (Static_noRotate != null) {
            Static_noRotate.scrollTo(position);
        }
    }

    @Override
    public void onBackgroundScrolled(int position) {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() != position) {
            playAtSongPosition(position);
        }
    }

    public void openNavDrawer() {
        bottomNowPlayingMenu = new BottomNowPlayingMenu();
        bottomNowPlayingMenu.setCallback(this::setUpNavigationView);
        bottomNowPlayingMenu.show(getSupportFragmentManager(), bottomNowPlayingMenu.tag);
    }

    private void setUpNavigationView(NavigationView navigationView) {
        if (navigationView == null) {
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        hsongName = headerView.findViewById(R.id.songNameHeader);
        hartistName = headerView.findViewById(R.id.artistNameHeader);
        halbumArt = headerView.findViewById(R.id.albumArtHeader);

        hsongName.setText(tempSong.songName);
        hartistName.setText(tempSong.artistName
        );
            halbumArt.setCornerRadius(24);
            halbumArt.setRoundedCorners(CORNER_ALL);
            halbumArt.setImageBitmap(tempAlbum);


        int textColorPrimary = ThemeUtils.getThemeTextColorPrimary(NowPlayingActivity.this);
        hsongName.setTextColor(textColorPrimary);
        hartistName.setTextColor(textColorPrimary);

        int[][] state = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_pressed}
        };

        int[] color = new int[]{
                getThemeAccentColor(NowPlayingActivity.this),
                getThemeTextColorPrimary(NowPlayingActivity.this),
                getThemeTextColorPrimary(NowPlayingActivity.this),
                getThemeTextColorPrimary(NowPlayingActivity.this),
                getThemeTextColorPrimary(NowPlayingActivity.this)
        };

        ColorStateList colorStateList = new ColorStateList(state, color);

        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);

        navigationView.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(NowPlayingActivity.this));

        navigationView.getMenu().getItem(0).setChecked(true);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (bottomNowPlayingMenu != null) {
                bottomNowPlayingMenu.dismiss();
            }

            new Handler().postDelayed(() -> {
                Song song = getCurrentSong();
                switch (item.getItemId()) {
                    case R.id.action_add_to_playlist: {
                        showAddToPlaylistDialog(NowPlayingActivity.this, song, false);
                        break;
                    }
                    case R.id.action_share: {
                        shareSong(NowPlayingActivity.this, song.getPath());
                        break;
                    }

                    case R.id.action_edit: {
                        Intent intent = new Intent(NowPlayingActivity.this, EditMetaDataActivity.class);
                        intent.putExtra("PATH", getCurrentSong().getPath());
                        intent.putExtra("ID", getCurrentSong().getId());
                        intent.putExtra("AlBUM_ID", getCurrentSong().getAlbumId());
                        startActivity(intent);
                        break;
                    }


            }
            }, 350);
            return true;
        });


        onEvent(EventBus.getDefault().getStickyEvent(AlbumArt.class));
        onEvent(EventBus.getDefault().getStickyEvent(CurrentPlayingSong.class));
    }

    private void showLyrics() {
        if (bottomSheetLyricsFragment == null) {
            bottomSheetLyricsFragment = new BottomSheetLyricsFragment();
            bottomSheetLyricsFragment.setCallback(nowPlayingLyrics -> {
                if (nowPlayingLyrics != null)
                {
                    this.nowPlayingLyrics = nowPlayingLyrics;
                    this.nowPlayingLyrics.setClickEventListener(this);
                    setLyrics();
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
                builder.setToolbarColor(ThemeUtils.getThemePrimaryColor(NowPlayingActivity.this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(NowPlayingActivity.this, Uri.parse(url));
        } catch (Exception e) {
            postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
            e.printStackTrace();
        }
    }


    @Override
    public void onDeleteClicked() {
        Song song = getCurrentSong();
        deleteLyricsFromCache(this,song.getId());
        LyricsUtils.deleteLyricsFromCache(this,song.getId());
        LyricsUtils.setLyricsInCache(this,"",song.getId());
        setLyrics();
        lyricsText = null;
        LineB1.setText("");
        Line00.setText("No Lyrics Found");
        Line01.setText("");
        Lines = null;


        //openFileIntent("*/*", 2);
    }

    @Override
    public void onSearchDatabaseClicked() {
        bottomSheetLyricsFragment.dismiss();
        Song song = getCurrentSong();
        Intent intent = new Intent(NowPlayingActivity.this,ShowDatabaseActivity.class);
        startActivity(intent);
    }


    @Override
    public void onAddClicked() {
        Song song = getCurrentSong();
        openFileIntent("*/*", 2);
    }

    private void setLyrics() {
        if (nowPlayingLyrics == null) {
            return;
        }
        final String lyricsString = LyricsUtils.getLyrics(HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().get(getSongPosition()).getPath(), getApplicationContext(), getCurrentSong().getId());
        final String lyrics = getReadableLyrics(lyricsString);
        nowPlayingLyrics.setLyrics(getString(R.string.loading_lyrics));
        if (executorService == null) {
            return;
        }
        executorService.execute(() -> {
            try {

                    new Handler(Looper.getMainLooper()).post(() -> nowPlayingLyrics.setLyrics(lyrics));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Lines != null){

                LineB1.setText(getLine(msg.what,-1));
                Line00.setText(getLine(msg.what,0));
                Line01.setText(getLine(msg.what,1));
            }
        }
    };

    private TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {
           // if (mediaPlayer.isPlaying()){
                Message msg = new Message();
                msg.what = songProgress2.getProgress();
                handler.sendMessage(msg);
            //}
        }
    };



    private void openFileIntent(String MIME, int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MIME);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, ""), requestCode);
    }


    public String getLine(int millisecond, int offset){
        if (Lines.isEmpty()) return null;
        Vector<Integer> time = new Vector<>(Lines.keySet());
        int LinePtr = 0;
        for (; LinePtr < time.size(); LinePtr++){
            if (LinePtr + 1 >= time.size()) break;
            if ((millisecond > time.get(LinePtr)) && (millisecond < time.get(LinePtr + 1))) break;
        }
        if (LinePtr + offset < 0) return null;
        if (LinePtr + offset >= time.size()) return null;
        return Lines.get(time.get(LinePtr + offset));
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


    private String getReadableLyrics(String lyricsString)
    {
        TreeMap<Integer,String> temp = convertToTreeMap(lyricsString);
        String t = "";
        if(temp != null)
        {
            Iterator<HashMap.Entry<Integer, String>> itr = temp.entrySet().iterator();
            while(itr.hasNext())
            {
                HashMap.Entry<Integer, String> entry = itr.next();
                t = t + entry.getValue() + "\n";
            }

        }
        String t1 = "";
        if(t1.equals(t) || t.equals(""))
        {
            return getString(R.string.no_lyrics);
        }

        return t;

    }


    public void openFullLyricsLayout(View view)
    {
        Intent intent = new Intent(NowPlayingActivity.this, FullLyricsLayoutActivity.class);
        startActivity(intent);
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
                        Lines = convertToTreeMap(lyrics);
                        setLyrics();
                        flag =1;
                        break;
                    }
                }
                if(flag == 0)
                    Line00.setText("No Lyrics Found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}