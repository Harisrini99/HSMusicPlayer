package music.hs.com.materialmusicv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.customviews.library.LibraryBottomPlaybackController;
import music.hs.com.materialmusicv2.customviews.library.LibraryFrameLayout;
import music.hs.com.materialmusicv2.objects.events.Refresh;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class LibraryActivity extends AppCompatActivity implements View.OnClickListener
{



    @BindView(R.id.libraryFrameLayout)
    public LibraryFrameLayout libraryFrameLayout;
    @BindView(R.id.libraryBottomPlaybackController)
    LibraryBottomPlaybackController libraryBottomPlaybackController;

    @BindView(R.id.back_toolbar)
    ImageButton BackToolbar;
    @BindView(R.id.title)
    TextView Title;
    @BindView(R.id.menu_sort)
    ImageButton MenuSort;
    @BindView(R.id.toolbar_card)
    CardView Toolbar;
    @BindView(R.id.searchButton)
    ImageButton SearchButton;

    @BindView(R.id.rootview)
    RelativeLayout Rootview;


    int colorAccent;
    int colorPrimary;
    int colorContrast;
    private int page;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.fragment_library);
        setTheme(ThemeUtils.getTheme(this));
        ButterKnife.bind(this);
        setUpBottomController();
        Intent intent = getIntent();
        page = intent.getIntExtra("frag",0);
        setFragment(page);
        colorAccent = MainActivity.colorAccent;
        colorPrimary = MainActivity.colorPrimary;
        colorContrast = ContrastColor(colorPrimary);
        boolean t =ThemeUtils.isThemeDarkOrBlack();
        if(t)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        if(page == 0) {
            setColorFilter(colorAccent, BackToolbar, MenuSort, SearchButton);
            BackToolbar.setOnClickListener(this);
            MenuSort.setOnClickListener(this);
            Title.setText("All Tracks");
            Title.setTextColor(colorContrast);
            SearchButton.setOnClickListener(this);

            Toolbar.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
            Rootview.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
        }
        else if(page == 2)
        {
            setColorFilter(colorAccent, BackToolbar, MenuSort, SearchButton);
            BackToolbar.setOnClickListener(this);
            MenuSort.setVisibility(View.GONE);
            SearchButton.setOnClickListener(this);
            Title.setText("Albums");
            Title.setTextColor(colorContrast);
            Toolbar.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
            Rootview.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
        }
        else if(page ==3 )
        {
            setColorFilter(colorAccent, BackToolbar, MenuSort, SearchButton);
            BackToolbar.setOnClickListener(this);
            MenuSort.setVisibility(View.GONE);
            SearchButton.setOnClickListener(this);
            //Title.setText("Artists");
            Title.setText("Artists");
            Title.setTextColor(colorContrast);
            Toolbar.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
            Rootview.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
        }
        else
        {
            setColorFilter(colorAccent, BackToolbar, MenuSort, SearchButton);
            BackToolbar.setOnClickListener(this);
            MenuSort.setVisibility(View.GONE);
            SearchButton.setVisibility(View.GONE);
            Title.setText("Playlists");
            Title.setTextColor(colorContrast);
            Toolbar.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
            Rootview.setBackgroundColor(getThemeWindowBackgroundColor(LibraryActivity.this));
        }

    }




    private void setFragment(int position)
    {
        try {
            if (libraryFrameLayout != null)
            {
                libraryFrameLayout.setFragment(this, position);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void reload() {
        if (libraryFrameLayout != null) {
            libraryFrameLayout.reload();
        }
    }

    private void setUpBottomController() {
        if (libraryBottomPlaybackController == null) {
            return;
        }
        boolean mainScreenStyle = HSMusicApplication.getInstance().getPreferenceUtils().getMainScreenStyle();
        if (!mainScreenStyle) {
            libraryBottomPlaybackController.setVisibility(View.GONE);
            return;
        }
        libraryBottomPlaybackController.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(this));
        libraryBottomPlaybackController.setProgressColor(ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(this)), getThemeAccentColor(this));
        libraryBottomPlaybackController.setOnClickEventDetectedListener(new LibraryBottomPlaybackController.OnClickEventDetectedListener() {
            @Override
            public void onPlayPreviousClicked() {
                playPrevious();
            }

            @Override
            public void onPlayPauseClicked() {
                pauseOrResumePlayer();
            }

            @Override
            public void onPlayNextClicked() {
                playNext();
            }

            @Override
            public void onSongNameClicked() {
                openNowPlaying();
            }

            @Override
            public void onOpenPlayingQueueClicked() {
                openPlayingQueue();
            }
        });
    }

    private void openNowPlaying() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongs() != null && HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(this, NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, this, TOAST_INFO);
        }
    }

    private void openPlayingQueue() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongs() != null && HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(this, QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, this, TOAST_INFO);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.back_toolbar: {
                onBackPressed();
                break;
            }
            case R.id.menu_sort:
            {
                toolbarMenuClick();
                break;
            }
            case R.id.searchButton:
            {
                openSearchActivity();
                break;
            }
        }
    }

    private void toolbarMenuClick() {
                    PopupMenu popupMenu = new PopupMenu(LibraryActivity.this,MenuSort);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_songs_fragment, popupMenu.getMenu());
        switch (HSMusicApplication.getInstance().getPreferenceUtils().getSelectedMenu())
        {
            case 0: {
                popupMenu.getMenu().findItem(R.id.title).setChecked(true);
                break;
            }
            case 1: {
                popupMenu.getMenu().findItem(R.id.album).setChecked(true);
                break;
            }
            case 2: {
                popupMenu.getMenu().findItem(R.id.artist).setChecked(true);
                break;
            }
            case 3: {
                popupMenu.getMenu().findItem(R.id.year).setChecked(true);
                break;
            }
            case 4:
            {
                popupMenu.getMenu().findItem(R.id.date).setChecked(true);
                break;
            }

        }

        popupMenu.setOnMenuItemClickListener(item ->
        {
            switch (item.getItemId()) {
                case R.id.title: {
                    popupMenu.getMenu().findItem(R.id.title).setChecked(true);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.TITLE);
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(MediaStore.Audio.Media.TITLE);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSelectedMenu(0);
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
                case R.id.album: {
                    popupMenu.getMenu().findItem(R.id.album).setChecked(true);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.ALBUM);
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(MediaStore.Audio.Media.ALBUM);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSelectedMenu(1);
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
                case R.id.artist: {
                    popupMenu.getMenu().findItem(R.id.artist).setChecked(true);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.ARTIST);
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(MediaStore.Audio.Media.ARTIST);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSelectedMenu(2);
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
                case R.id.year: {
                    popupMenu.getMenu().findItem(R.id.year).setChecked(true);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.YEAR);
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(MediaStore.Audio.Media.YEAR);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSelectedMenu(3);
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
                case R.id.date: {
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.DATE_ADDED);
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(MediaStore.Audio.Media.DATE_ADDED);
                    HSMusicApplication.getInstance().getPreferenceUtils().setSelectedMenu(4);
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
                case R.id.reverse:
                {
                    String t = HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder();
                    HSMusicApplication.getInstance().getPreferenceUtils().setSongSortOrder(t+" Desc");
                    HSMusicApplication.getInstance().getPreferenceUtils().setAlbumSortOrder(t+" Desc");
                    EventBus.getDefault().post(new Refresh());
                    break;
                }
            }
            return true;
        });
                    popupMenu.show();

    }

    private void openSearchActivity()
    {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(LibraryActivity.this, SearchActivity.class);
        intent.putExtra("page",page);
        startActivity(intent, bundle);
    }
}


