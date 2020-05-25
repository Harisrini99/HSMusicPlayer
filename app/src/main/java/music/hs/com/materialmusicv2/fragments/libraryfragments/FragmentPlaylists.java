package music.hs.com.materialmusicv2.fragments.libraryfragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import music.hs.com.materialmusicv2.adapters.PlaylistAdapter;
import music.hs.com.materialmusicv2.objects.Playlist;
import music.hs.com.materialmusicv2.objects.events.Refresh;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showInputDialog;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.getPlaylistID;
import static music.hs.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;

public class FragmentPlaylists extends Fragment implements SwipeRefreshLayout.OnRefreshListener,View.OnClickListener {

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.target)
    FloatingActionButton TargetFab;


    @BindView(R.id.fab_shuffle_add)
    FloatingActionButton Fab_Shuffle_Add;


    private ArrayList<Playlist> playlists;
    private PlaylistAdapter playlistAdapter;

    private boolean animated = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recyclerview_layout_for_common_use, container, false);
        ButterKnife.bind(this, rootView);

        setUpRecyclerView(recyclerView, new LinearLayoutManager(getActivity()), Color.TRANSPARENT);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        swipeRefreshLayout.setOnRefreshListener(this);
        Fab_Shuffle_Add.setImageTintList(ColorStateList.valueOf(ThemeUtils.getThemePrimaryColor(getContext())));

        Fab_Shuffle_Add.setOnClickListener(this);
        Fab_Shuffle_Add.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getThemeAccentColor(getContext())));
        Fab_Shuffle_Add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
        TargetFab.setVisibility(View.GONE);

        loadPlaylists();

        return rootView;
    }

    private void loadPlaylists() {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            playlists = QueryUtils.getAllPlaylists(getActivity().getContentResolver(), MediaStore.Audio.Playlists.NAME, true);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() == null) {
                    return;
                }
                if (playlists != null && !playlists.isEmpty() && recyclerView != null) {
                    if (playlistAdapter == null) {
                        playlistAdapter = new PlaylistAdapter(playlists, getActivity());
                        recyclerView.setAdapter(playlistAdapter);
                    } else {
                        playlistAdapter.changePlaylists(playlists);
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
        if (playlists != null) {
            playlists.clear();
            playlists = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (playlistAdapter != null) {
            playlistAdapter.clear();
            playlistAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        loadPlaylists();
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

    @Override
    public void onClick(View v)
    {
        if(v == Fab_Shuffle_Add)
        {
            showInputDialog(getActivity(),
                    R.string.create_playlist,
                    R.string.enter_playlist_name,
                    input -> {
                        int isPlaylistCreated = createPlaylist(input, getActivity());
                        if (isPlaylistCreated == 1) {
                            long playlistID = getPlaylistID(input, getActivity());
                            playlists.add(new Playlist(playlistID, input));
                            //notifyItemInserted(playlists.size());
                            loadPlaylists();
                            postToast(R.string.playlist_created, getActivity(), TOAST_SUCCESS);
                        } else if (isPlaylistCreated == 0) {
                            postToast(R.string.playlist_exists, getActivity(), TOAST_INFO);
                        } else {
                            postToast(R.string.error_label, getActivity(), TOAST_ERROR);
                        }
                    });
        }
    }
}