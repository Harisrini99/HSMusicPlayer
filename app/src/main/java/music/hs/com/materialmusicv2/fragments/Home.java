package music.hs.com.materialmusicv2.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.activities.LibraryActivity;
import music.hs.com.materialmusicv2.activities.NowPlayingActivity;
import music.hs.com.materialmusicv2.activities.QueueActivity;
import music.hs.com.materialmusicv2.adapters.PlaylistAdapter;
import music.hs.com.materialmusicv2.adapters.SongAdapter;
import music.hs.com.materialmusicv2.customviews.library.LibraryBottomPlaybackController;
import music.hs.com.materialmusicv2.objects.Album;
import music.hs.com.materialmusicv2.objects.Playlist;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.misc.DragSortRecycler;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.hs.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeColorControlHighlight;

public class Home extends Fragment implements View.OnClickListener
{


    @BindView(R.id.libraryBottomPlaybackController)
    LibraryBottomPlaybackController libraryBottomPlaybackController;

    @BindView(R.id.track_image)
    ImageView TrackImage;
    @BindView(R.id.album_image)
    ImageView PlayListImage;
    @BindView(R.id.artist_image)
    ImageView ArtistImage;

    @BindView(R.id.all_track)
    TextView AllTrack;
    @BindView(R.id.album)
    TextView PlayList;
    @BindView(R.id.artist)
    TextView Artist;

    @BindView(R.id.count1)
    TextView Count1;
    @BindView(R.id.count2)
    TextView Count2;
    @BindView(R.id.count3)
    TextView Count3;

    @BindView(R.id.all_tracks_layout)
    LinearLayout AllTrackLayout;
    @BindView(R.id.album_layout)
    LinearLayout PlayListLayout;
    @BindView(R.id.artist_layout)
    LinearLayout ArtistLayout;

    @BindView(R.id.recent_title)
    TextView RecentTitle;
    @BindView(R.id.recent_button)
    LinearLayout RecentButton;
    @BindView(R.id.ri_arrow)
    ImageButton Right_Arrow;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private SongAdapter songAdapter;
    DragSortRecycler dragSortRecycler;
    private ArrayList<Song> songs;



    int colorAccent;
    int colorPrimary;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, rootView);

        colorAccent = ThemeUtils.getThemeAccentColor(getContext());
        colorPrimary = ThemeUtils.getThemePrimaryColor(getContext());

        setColorFilter(colorAccent,TrackImage,PlayListImage,Right_Arrow,ArtistImage);
        AllTrack.setTextColor(ContrastColor(colorPrimary));
        PlayList.setTextColor(ContrastColor(colorPrimary));
        Artist.setTextColor(ContrastColor(colorPrimary));
        //Count1.setTextColor(ContrastColor(colorPrimary));
        //Count2.setTextColor(ContrastColor(colorPrimary));
        //Count3.setTextColor(ContrastColor(colorPrimary));
        RecentTitle.setTextColor(colorAccent);
        RecentButton.setOnClickListener(this);
        Right_Arrow.setOnClickListener(this);
        AllTrackLayout.setOnClickListener(this);
        PlayListLayout.setOnClickListener(this);
        ArtistLayout.setOnClickListener(this);




        load();
        setUpBottomController();
        return rootView;
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
        libraryBottomPlaybackController.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity()));
        libraryBottomPlaybackController.setProgressColor(ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity())), getThemeAccentColor(getActivity()));
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
            Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }

    private void openPlayingQueue() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongs() != null && HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(getActivity(), QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.all_tracks_layout:
                {
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                intent.putExtra("frag", 0);
                getActivity().startActivity(intent);
                break;

            }
            case R.id.album_layout:
                {
                    Intent intent = new Intent(getActivity(), LibraryActivity.class);
                    intent.putExtra("frag", 2);
                    getActivity().startActivity(intent);
                    break;
            }
            case R.id.artist_layout:
            {
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                intent.putExtra("frag", 3);
                getActivity().startActivity(intent);
                break;
            }
            case R.id.recent_button:
            {
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                intent.putExtra("frag", 4);
                getActivity().startActivity(intent);
                break;
            }
            case  R.id.ri_arrow:
            {

                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                intent.putExtra("frag", 4);
                getActivity().startActivity(intent);
                break;
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load()
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        ArrayList<Playlist> count2List = QueryUtils.getAllPlaylists(contentResolver,"",true);
        ArrayList<Album> album = QueryUtils.getAllAlbums(contentResolver);
        ArrayList<Song>  count1List = QueryUtils.getAllSongs(contentResolver,"");
        ArrayList<music.hs.com.materialmusicv2.objects.Artist> count3List = QueryUtils.getAllArtists(contentResolver);
        Count1.setText("( "+count1List.size()+" )");
        Count2.setText("( "+album.size()+" )");
        Count3.setText("( "+count3List.size()+" )");
        recyclerView.setItemAnimator(null);
        RecentTitle.setText("Playlists ( "+count2List.size()+" )");
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getSongs();
        dragSortRecycler = new DragSortRecycler(getThemeColorControlHighlight(getActivity()));
        recyclerView.removeItemDecoration(dragSortRecycler);
        recyclerView.removeOnItemTouchListener(dragSortRecycler);
        recyclerView.removeOnScrollListener(dragSortRecycler.getScrollListener());
        if (!songs.isEmpty())
        {
            int n = songs.size();
            if(n > 7)
             {
                songs.subList(7,n).clear();
             }
               //songAdapter = new SongAdapter(songs, getActivity(), false,false);

        }
        PlaylistAdapter p = new PlaylistAdapter(count2List,getActivity());
        recyclerView.setAdapter(p);
        recyclerView.setLayoutFrozen(true);
        recyclerView.setScrollContainer(false);
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);

    }

    private void getSongs()
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        songs = QueryUtils.getLastPlayedSongs(getActivity());
        songs = QueryUtils.removeNonExistentSongs(songs);
    }


}