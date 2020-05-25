package music.hs.com.materialmusicv2.fragments.libraryfragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import music.hs.com.materialmusicv2.adapters.ArtistAdapter;
import music.hs.com.materialmusicv2.objects.Artist;
import music.hs.com.materialmusicv2.objects.events.Refresh;
import music.hs.com.materialmusicv2.objects.events.RefreshAdapter;
import music.hs.com.materialmusicv2.objects.events.RefreshGrid;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.hs.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.hs.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class FragmentArtists extends Fragment implements SwipeRefreshLayout.OnRefreshListener,View.OnClickListener {

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.fab_shuffle_add)
    FloatingActionButton Fab_Shuffle_Add;
    @BindView(R.id.target)
    FloatingActionButton TargetFab;


    private ArrayList<Artist> artists;
    private ArtistAdapter artistAdapter;

    private int gridSize;

    private boolean animated = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recyclerview_layout_for_common_use, container, false);
        ButterKnife.bind(this, rootView);

        //gridSize = HSMusicApplication.getInstance().getPreferenceUtils().getArtistGrid();
        gridSize = 2;
        setUpRecyclerView(recyclerView, new GridLayoutManager(getActivity(), gridSize), getThemeAccentColor(getActivity()));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        swipeRefreshLayout.setOnRefreshListener(this);
        Fab_Shuffle_Add.setImageTintList(ColorStateList.valueOf(ThemeUtils.getThemePrimaryColor(getContext())));
        Fab_Shuffle_Add.setOnClickListener(this);
        Fab_Shuffle_Add.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getThemeAccentColor(getContext())));
        TargetFab.setVisibility(View.GONE);
        loadArtists();

        return rootView;
    }

    private void loadArtists() {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            artists = QueryUtils.getAllArtists(getActivity().getContentResolver());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() == null) {
                    return;
                }
                if (artists != null && !artists.isEmpty() && recyclerView != null) {
                    if (artistAdapter == null) {
                        artistAdapter = new ArtistAdapter(artists, getActivity());
                        recyclerView.setAdapter(artistAdapter);
                    } else {
                        artistAdapter.changeArtists(artists);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
                if (!animated) {
                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
                    recyclerView.setLayoutAnimation(animation);
                    animated = true;
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        if (artists != null) {
            artists.clear();
            artists = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        artistAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        loadArtists();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshGrid refreshGrid) {
        refreshGrid();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshAdapter refreshAdapter) {
        refreshAdapter();
    }

    private void refreshGrid() {
        if (recyclerView == null) {
            return;
        }
        if (gridSize == HSMusicApplication.getInstance().getPreferenceUtils().getArtistGrid()) {
            return;
        }
        int prevGridSize = gridSize;
        gridSize = HSMusicApplication.getInstance().getPreferenceUtils().getArtistGrid();
        if (recyclerView.getLayoutManager() != null) {
            int gridSize = HSMusicApplication.getInstance().getPreferenceUtils().getArtistGrid();
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(gridSize);
        }
        if (gridSize == 1 || prevGridSize == 1) {
            artistAdapter = null;
            onRefresh();
        }
    }

    private void refreshAdapter() {
        artistAdapter = null;
        onRefresh();
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
    }
}