package music.hs.com.materialmusicv2.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomNavigationDrawerFragment;
import music.hs.com.materialmusicv2.customviews.library.LibraryController;
import music.hs.com.materialmusicv2.customviews.library.LibraryToolbar;
import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.hs.com.materialmusicv2.fragments.FragmentFolders;
import music.hs.com.materialmusicv2.fragments.Home;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.AlbumArt;
import music.hs.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.hs.com.materialmusicv2.objects.events.PlaybackPosition;
import music.hs.com.materialmusicv2.objects.events.PlaybackState;
import music.hs.com.materialmusicv2.objects.events.RepeatState;
import music.hs.com.materialmusicv2.objects.events.ShuffleState;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlaySongAtStart;
import music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.hs.com.materialmusicv2.utils.fileutils.RealPathUtil;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView.CORNER_ALL;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.controller.Controller.changeRepeat;
import static music.hs.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class MainActivity extends MusicPlayerActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    int currentFragment = 0;

    @Nullable
    @BindView(R.id.libraryToolbar)
    public LibraryToolbar libraryToolbar;
    @BindView(R.id.libraryController)
    LibraryController libraryController;
    @BindView(R.id.elementsContainer)
    FrameLayout elementsContainer;

    private BottomNavigationDrawerFragment bottomNavigationDrawerFragment;

    private TextView songName;
    private TextView artistName;
    private RoundedSquareImageView albumArt;
    static int flag;
    private FragmentFolders fragmentFolders;
    private Home fragmentHome;
    private static Activity activity;

    private float dp;
    private float dX;
    private float dY;
    private int lastAction;
    private float idealXPosition;
    private boolean isLibraryControllerHidden = false;
    public static int colorPrimary;
    public static int colorAccent;

    private boolean mainScreenStyle;
    public CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (!HSMusicApplication.getInstance().getPreferenceUtils().getIsIntroShown()) {
            Intent intent = new Intent(MainActivity.this, HSIntroActivity.class);
            startActivity(intent);
        }

        setTheme(ThemeUtils.getTheme(this));

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        mainScreenStyle = HSMusicApplication.getInstance().getPreferenceUtils().getMainScreenStyle();

        setContentView(R.layout.activity_main);
        activity = this;

        boolean t =ThemeUtils.isThemeDarkOrBlack();
        if(t)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        colorPrimary= ThemeUtils.getThemePrimaryColor(this);
        colorAccent = ThemeUtils.getThemeAccentColor(this);
        ButterKnife.bind(this);


        if (HSMusicApplication.getInstance().isMiui())
        {
            ThemeUtils.setDarkStatusBarIcons(MainActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        if (libraryToolbar != null)
        {
            libraryToolbar.setTitle(getString(R.string.search_your_library));
            libraryToolbar.setHamburgerMenuClick(this::openNavDrawer);
            libraryToolbar.setSleepButtonClick(this::openSleepTimer);
            libraryToolbar.setTitleClick(() -> {
                if (currentFragment == 0) {
                    openSearchActivity();
                }
            });
            libraryToolbar.setGone();
            libraryToolbar.setColorToTimer();
            libraryToolbar.setBackgroundColor(getThemeWindowBackgroundColor(MainActivity.this));
        }



        setFragment(0);

        setUpLibraryController();

        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        handleIntent(getIntent());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void setFragment(int index)
    {

        if (index == 0 && fragmentHome != null)
        {
            return;
        }
        if (index == 1 && fragmentFolders != null)
        {
            return;
        }

        currentFragment = index;
        Fragment fragment = null;
        if (index == 0)
        {
            fragmentHome = new Home();
            fragment = fragmentHome;
            fragmentFolders = null;
        }
        else if (index == 1)
        {
            fragmentFolders = new FragmentFolders();
            fragment = fragmentFolders;
            fragmentHome = null;
        }


        if (fragment != null)
        {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.elementsContainer, fragment);
            fragmentTransaction.commit();
        }

        if (libraryToolbar != null)
        {
            if (index == 0)
            {
                libraryToolbar.setTitle(getString(R.string.search_your_library));
            }
            else if (index == 1) {
                libraryToolbar.setTitle(getString(R.string.folders_title));
            }
        }

        HSMusicApplication.getInstance().getPreferenceUtils().setLastOpenedFragment(currentFragment);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpLibraryController() {
        if (libraryController == null)
        {
            return;
        }

        if (mainScreenStyle) {
            libraryController.setVisibility(View.GONE);
            return;
        }

        libraryController.setOnClickEventDetectedListener(new LibraryController.OnClickEventDetectedListener() {
            @Override
            public void onShuffleClicked() {
            }

            @Override
            public void onPlayPreviousClicked() {
                playPrevious();
                resetClipping();
            }

            @Override
            public void onPlayPauseClicked() {
                pauseOrResumePlayer();
                resetClipping();
            }

            @Override
            public void onPlayNextClicked() {
                playNext();
                resetClipping();
            }

            @Override
            public void onRepeatClicked() {
                changeRepeat();
                resetClipping();
            }

            @Override
            public void onBodyClicked() {
                openNowPlaying();
            }
        });

        if (!mainScreenStyle) {
            float width = getResources().getDisplayMetrics().widthPixels;
            dp = getResources().getDisplayMetrics().density;
            idealXPosition = width - 80 * dp;

            if (clipHandler != null) {
                clipHandler.postDelayed(clipRunnable, 2500);
            }
        }

        libraryController.setOnTouchListener(this);
    }

    private void openNowPlaying() {

        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongs() != null && HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {

            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getApplicationContext(), TOAST_INFO);
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (mainScreenStyle) {
            return;
        }

        if (!isLibraryControllerHidden) {
            if (clipHandler != null) {
                clipHandler.postDelayed(clipRunnable, 2500);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment == 1 && fragmentFolders != null) {
            if (!fragmentFolders.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumArt albumArt) {
        if (albumArt == null || albumArt.albumArt == null) {
            if (this.albumArt != null) {
                this.albumArt.setVisibility(View.GONE);
            }
            return;
        }
        if (this.albumArt != null) {
            this.albumArt.setVisibility(View.VISIBLE);
            this.albumArt.setCornerRadius(24);
            this.albumArt.setRoundedCorners(CORNER_ALL);
            this.albumArt.setImageBitmap(albumArt.albumArt);
        }
        if (libraryController != null) {
            libraryController.onBitmapChanged(albumArt.albumArt);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState) {
        if (libraryController != null) {
            libraryController.setPlayPause(playbackState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RepeatState repeatState) {
        if (libraryController != null) {
            libraryController.setRepeat(repeatState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ShuffleState shuffleState) {
        if (libraryController != null)
        {
            libraryController.setShuffle(shuffleState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition) {
        if (libraryController != null) {
            libraryController.setPlaybackPosition(playbackPosition.position, playbackPosition.duration);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong) {
        if (currentPlayingSong == null) {
            if (this.songName != null) {
                this.songName.setVisibility(View.GONE);
            }
            if (this.artistName != null) {
                this.artistName.setVisibility(View.GONE);
            }
            return;
        }
        if (this.songName != null) {
            this.songName.setVisibility(View.VISIBLE);
            this.songName.setText(currentPlayingSong.songName);
        }
        if (this.artistName != null) {
            this.artistName.setVisibility(View.VISIBLE);
            this.artistName.setText(currentPlayingSong.artistName);
        }
        if (libraryController != null) {
            libraryController.setSongName(currentPlayingSong.songName);
        }
    }

    public void openNavDrawer() {
        bottomNavigationDrawerFragment = new BottomNavigationDrawerFragment();
        bottomNavigationDrawerFragment.setCallback(this::setUpNavigationView);
        bottomNavigationDrawerFragment.show(getSupportFragmentManager(), bottomNavigationDrawerFragment.tag);
    }

    public void openSleepTimer()
    {
        flag = 0;
        if(countDownTimer != null)
        {
            countDownTimer.cancel();
        }

        long[] t = new long[5];
        DialogUtils.showSeekbarInputDialog(MainActivity.this,
                R.string.sleep_timer,
                R.string.sleep_timer_hint,
                1,
                60,
                flag,
                input -> {
                    try {
                        t[0] = Long.parseLong(input) * 60 * 1000;
                        setTimer(t[0]);
                        int col = Color.parseColor("#F05246");
                        libraryToolbar.setColorForSleep(col);
                        libraryToolbar.setVisble();
                        int colorPrimary = ThemeUtils.getThemeTextColorPrimary(this);
                        setTimer(t[0]);
                        postToast(R.string.sleep_timer_set, getApplicationContext(), TOAST_SUCCESS);
                    } catch (Exception e) {
                        postToast(e.toString(), getApplicationContext(), TOAST_ERROR);
                    }
                }
        );

    }

    private void setUpNavigationView(NavigationView navigationView) {
        if (navigationView == null) {
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        songName = headerView.findViewById(R.id.songNameHeader);
        artistName = headerView.findViewById(R.id.artistNameHeader);
        albumArt = headerView.findViewById(R.id.albumArtHeader);




        int textColorPrimary = ThemeUtils.getThemeTextColorPrimary(MainActivity.this);
        songName.setTextColor(textColorPrimary);
        artistName.setTextColor(textColorPrimary);

        int[][] state = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_pressed}
        };

        int[] color = new int[]{
                getThemeAccentColor(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this)
        };

        ColorStateList colorStateList = new ColorStateList(state, color);

        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);

        navigationView.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(MainActivity.this));

        navigationView.getMenu().getItem(currentFragment).setChecked(true);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (bottomNavigationDrawerFragment != null) {
                bottomNavigationDrawerFragment.dismiss();
            }
            new Handler().postDelayed(() -> {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                    {
                        setFragment(0);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.nav_folder: {
                        setFragment(1);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.nav_settings: {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_equalizer: {
                        Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_artwork:
                    {
                        Intent intent = new Intent(this,CreateArtWork.class);
                        startActivity(intent);
                        break;
                    }

                    case R.id.nav_firebase:
                    {
                       uploadLyrics();
                        break;
                    }

                }
            }, 350);
            return true;
        });


        onEvent(EventBus.getDefault().getStickyEvent(AlbumArt.class));
        onEvent(EventBus.getDefault().getStickyEvent(CurrentPlayingSong.class));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "language":
            case "theme":
            case "imageType":
            case "accentColor":
            case "donated":
            case "mainScreenStyle":
            case "defaultPage":
            case "isIntroEnabled":
            case "colorizeElementsAccordingToAlbumArt":
            case "tabTitlesMode":
            case "songItemStyle":
            case "genreItemStyle":
            case "playlistItemStyle":
            case "useCircularImage":
                recreate();
                break;
            case "albumGrid":

        }
    }

    private Handler clipHandler = new Handler();

    private Runnable clipRunnable = () -> {
        if (libraryController == null) {
            return;
        }

        float dx = libraryController.getLeft();

        ObjectAnimator animation = ObjectAnimator.ofFloat(libraryController, "translationX", idealXPosition - dx);
        animation.setDuration(500);
        animation.start();

        isLibraryControllerHidden = true;
    };

    private float startX = -1;
    private float startY = -1;

    private long startTime = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(@NonNull View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                startX = motionEvent.getRawX();
                startY = motionEvent.getRawY();

                dX = view.getX() - motionEvent.getRawX();
                dY = view.getY() - motionEvent.getRawY();

                startTime = System.currentTimeMillis();

                lastAction = MotionEvent.ACTION_DOWN;
                if (clipHandler != null) {
                    clipHandler.removeCallbacks(clipRunnable);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                isLibraryControllerHidden = false;

                float libraryControllerHeight = view.getHeight();
                float bottom = elementsContainer.getBottom();

                if (!mainScreenStyle) {
                    view.setY(Math.max(0, Math.min(motionEvent.getRawY() + dY, bottom - libraryControllerHeight - 72 * dp)));
                    view.setX(Math.max(0, motionEvent.getRawX() + dX));
                }

                lastAction = MotionEvent.ACTION_MOVE;
                break;
            }

            case MotionEvent.ACTION_UP: {
                float endX = motionEvent.getRawX();
                float endY = motionEvent.getRawY();

                if (lastAction == MotionEvent.ACTION_DOWN || (isAClick(startX, endX, startY, endY) && (System.currentTimeMillis() - startTime <= 100))) {
                    if (Math.abs(idealXPosition - libraryController.getX()) < 100) {
                        ObjectAnimator animation = ObjectAnimator.ofFloat(libraryController, "translationX", 0);
                        animation.setDuration(500);
                        animation.start();
                        isLibraryControllerHidden = false;
                        if (clipHandler != null) {
                            clipHandler.postDelayed(clipRunnable, 2500);
                        }
                    } else {
                        openNowPlaying();
                    }
                } else {
                    if (clipHandler != null) {
                        clipHandler.postDelayed(clipRunnable, 2500);
                    }
                }
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onDestroy() {
        try {
            bottomNavigationDrawerFragment = null;
            HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        try {
            if (intent.getBooleanExtra("openNowPlaying", false)) {
                openNowPlaying();
            } else {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                        String type = intent.getType();
                        if (type != null) {
                            Uri uri = intent.getData();
                            EventBus.getDefault().postSticky(new PlaySongAtStart(RealPathUtil.getRealPath(getApplicationContext(), uri)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0)
        {
            if (!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ))
            {
                setFragment(HSMusicApplication.getInstance().getPreferenceUtils().getLastOpenedFragment());
            }
        }
    }

    public void resetClipping() {
        if (mainScreenStyle) {
            return;
        }
        if (clipHandler != null) {
            clipHandler.removeCallbacks(clipRunnable);
            clipHandler.postDelayed(clipRunnable, 2500);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (currentFragment == 1 && fragmentFolders != null) {
            fragmentFolders.onRestore();
        }
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > 10 || differenceY > 10);
    }


    private void openSearchActivity() {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent, bundle);
    }

    private void uploadLyrics()
    {
      ProgressDialog  progressDialog = new ProgressDialog(this);
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

    private void setTimer(long t)
    {
        int col = ThemeUtils.getThemeTextColorPrimary(this);
        if(countDownTimer != null)
        {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(t, 1000)
                {
                    public void onTick(long millisUntilFinished) {
                        String time_to_print = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                        //libraryToolbar.setAnim();
                        libraryToolbar.setTextViewTitle("("+time_to_print+")");
                        flag = 1;
                    }

                    @Override
                    public void onFinish() {
                        libraryToolbar.setGone();
                        libraryToolbar.setColorForSleep(col);
                        flag = 0;
                    }

                }.start();


    }

    public void stopTimer()
    {
        int col = ThemeUtils.getThemeTextColorPrimary(this);
        if(countDownTimer != null)
        {
            countDownTimer.cancel();
        }
        libraryToolbar.setGone();
        libraryToolbar.setColorForSleep(col);
        flag = 0;
    }

}

