package music.hs.com.materialmusicv2.fragments.libraryfragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.activities.MainActivity;
import music.hs.com.materialmusicv2.adapters.SongAdapter;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.hs.com.materialmusicv2.objects.events.Refresh;
import music.hs.com.materialmusicv2.objects.events.SongListChanged;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.hs.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.hs.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class FragmentSongs extends Fragment implements SwipeRefreshLayout.OnRefreshListener,View.OnClickListener {

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    FastScrollRecyclerView recyclerView;

    @BindView(R.id.target)
    FloatingActionButton TargetFab;
    @BindView(R.id.fab_shuffle_add)
    FloatingActionButton Fab_Shuffle_Add;

    static int position;
    private ArrayList<Song> songs;
    private SongAdapter songAdapter;

    private boolean animated = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recyclerview_layout_for_common_use, container, false);
        ButterKnife.bind(this, rootView);
        setUpRecyclerView(recyclerView, new LinearLayoutManager(getActivity()), getThemeAccentColor(getActivity()));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        swipeRefreshLayout.setOnRefreshListener(this);
        Fab_Shuffle_Add.setOnClickListener(this);
        Fab_Shuffle_Add.setImageTintList(ColorStateList.valueOf(ThemeUtils.getThemePrimaryColor(getContext())));
        Fab_Shuffle_Add.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getThemeAccentColor(getContext())));
        TargetFab.setOnClickListener(this);
        loadSongs();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && TargetFab.getVisibility() == View.VISIBLE) {
                    TargetFab.hide();
                } else if (dy < 0 && TargetFab.getVisibility() != View.VISIBLE) {
                    TargetFab.show();
                }
            }
        });

        songs = QueryUtils.getAllSongs(getActivity().getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
        ArrayList<Song> t = QueryUtils.getLastPlayedSongs(getContext());
        if( t.size() > 0) {
            long id = t.get(0).getId();
            int i = 0;
            for (Song s : songs) {
                if (s.getId() == id) {
                    break;
                }
                i++;
            }
            position = i;
        }

        return rootView;
    }

    private void loadSongs()
    {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            songs = QueryUtils.getAllSongs(getActivity().getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() == null) {
                    return;
                }
                if (songs != null && !songs.isEmpty())
                {
                    if (songAdapter == null)
                    {
                        songAdapter = new SongAdapter(songs, getActivity(), false,true);
                        recyclerView.setAdapter(songAdapter);
                        recyclerView.smoothScrollToPosition(0);
                        recyclerView.scrollToPosition(0);
                    }
                    else
                        {
                            songAdapter.changeSongs(songs);
                            recyclerView.smoothScrollToPosition(0);
                            recyclerView.scrollToPosition(0);
                    }


                }
                swipeRefreshLayout.setRefreshing(false);
                if (!animated)
                {
                    //LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
                    //recyclerView.setLayoutAnimation(animation);
                    //recyclerView.smoothScrollToPosition(0);
                    //recyclerView.scrollToPosition(0);
                    animated = true;
                }
            });
        });
    }

    private void reLoadSongs()
    {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            songs = QueryUtils.getAllSongs(getActivity().getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() == null) {
                    return;
                }
                if (songs != null && !songs.isEmpty())
                {
                        songAdapter = new SongAdapter(songs, getActivity(), false,true);
                        recyclerView.setAdapter(songAdapter);
                        recyclerView.smoothScrollToPosition(0);
                        recyclerView.scrollToPosition(0);

                }
                swipeRefreshLayout.setRefreshing(false);
                if (!animated)
                {
                  //LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
                   //recyclerView.setLayoutAnimation(animation);
                    //recyclerView.smoothScrollToPosition(0);
                    //recyclerView.scrollToPosition(0);
                    animated = true;
                }
            });
        });
    }


    @Override
    public void onDestroy() {
        if (songs != null) {
            songs.clear();
            songs = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (songAdapter != null) {
            songAdapter.clear();
            songAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh()
    {
        reLoadSongs();
        recyclerView.smoothScrollToPosition(0);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        onRefresh();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong)
    {
        long id = currentPlayingSong.id;
        int i =0;
        for(Song s : songs)
        {
            if(s.getId() == id)
            {
                break;
            }
            i++;
        }
        position = i;
    }


    @Override
    public void onClick(View v)
    {
        if(v == Fab_Shuffle_Add)
        {
            changeShuffle();
            shuffleList(QueryUtils.getAllSongs(getActivity().getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder()));
            new MainActivity().resetClipping();
        }

        if(v == TargetFab)
        {
            //Toast.makeText(getContext(),position+"--------"+songs.get(position), Toast.LENGTH_LONG).show();
            recyclerView.scrollToPosition(position);
        }
    }
}